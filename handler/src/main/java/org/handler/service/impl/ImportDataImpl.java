package org.handler.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.handler.dto.request.ContractRequestDto;
import org.handler.service.ImportDataService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ImportDataImpl implements ImportDataService {
    private final VectorStore vectorStore;
    private static final int BATCH_SIZE = 100;
    private long totalDocuments = 0;
    private long insertedDocuments = 0;
    private long skippedDocuments = 0;
    private static final Logger logger = LoggerFactory.getLogger(ImportDataImpl.class);

    @Override
    public void importFile(Path path) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<Document> documents = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;

            while ((line = reader.readLine()) != null) {
                totalDocuments++;
                ContractRequestDto dto = mapper.readValue(line, ContractRequestDto.class);
                Document document = toDocument(dto);

                documents.add(document);

                if (documents.size() >= BATCH_SIZE) {
                    safeBatchInsert(documents);
                    documents.clear();
                }
            }

            if (!documents.isEmpty()) {
                safeBatchInsert(documents);
            }
        } finally {
            logger.info("Import finished. Total={}, Inserted={}, Skipped={}", totalDocuments, insertedDocuments, skippedDocuments);
        }
    }

    @Override
    public void transformAndSaveJsonl(Path inputPath, Path outputPath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        try (BufferedReader reader = Files.newBufferedReader(inputPath);
             BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                JsonNode root = mapper.readTree(line);
                ObjectNode transformed = transformTenderJson(root, mapper);
                writer.write(mapper.writeValueAsString(transformed));
                writer.newLine();
            }
        }

        logger.info("Transformation finished. Output saved to {}", outputPath);
    }

    private ObjectNode transformTenderJson(JsonNode root, ObjectMapper mapper) {
        ObjectNode result = mapper.createObjectNode();

        // ------------------------
        // 1. EMBEDDING TEXT
        // ------------------------

        StringBuilder embeddingText = new StringBuilder();
        Set<String> seenTexts = new HashSet<>();

        JsonNode tender = root.path("tender");

        addUniqueText(seenTexts, embeddingText, tender.path("description").asText(null));
        addUniqueText(seenTexts, embeddingText, tender.path("title").asText(null));

        JsonNode lots = tender.path("lots");

        if (lots.isArray()) {
            for (JsonNode lot : lots) {
                addUniqueText(seenTexts, embeddingText, lot.path("description").asText(null));
                addUniqueText(seenTexts, embeddingText, lot.path("title").asText(null));
            }
        }

        String cleanedEmbedding = embeddingText
                .toString()
                .replaceAll("\\s+", " ")
                .trim();

        if (cleanedEmbedding.length() > 30000) {
            cleanedEmbedding = cleanedEmbedding.substring(0, 30000);
        }

        result.put("embedding_text", cleanedEmbedding);

        // ------------------------
        // 2. BUYER
        // ------------------------

        result.put("buyer", root.path("buyer").path("name").asText(""));

        // ------------------------
        // 3. SUPPLIERS
        // ------------------------

        ArrayNode suppliersArray = mapper.createArrayNode();
        Set<String> seenSuppliers = new HashSet<>();

        JsonNode parties = root.path("parties");

        if (parties.isArray()) {
            for (JsonNode party : parties) {

                JsonNode roles = party.path("roles");

                boolean isSupplier = false;

                if (roles.isArray()) {
                    for (JsonNode role : roles) {
                        if ("supplier".equalsIgnoreCase(role.asText())) {
                            isSupplier = true;
                            break;
                        }
                    }
                }

                if (isSupplier) {
                    String name = party.path("name").asText("");

                    if (!name.isBlank() && seenSuppliers.add(name)) {
                        ObjectNode supplierNode = mapper.createObjectNode();
                        supplierNode.put("name", name);
                        JsonNode address = party.path("address");
                        if (!address.isMissingNode()) {
                            supplierNode.set("address", address);
                        }
                        suppliersArray.add(supplierNode);
                    }
                }
            }
        }

        result.set("suppliers", suppliersArray);

        // ------------------------
        // 4. CPV CODES
        // ------------------------

        Set<String> cpvCodes = new HashSet<>();
        JsonNode items = tender.path("items");

        if (items.isArray()) {
            for (JsonNode item : items) {
                JsonNode classification = item.path("classification");
                if ("CPV".equalsIgnoreCase(classification.path("scheme").asText())) {
                    String cpvId = classification.path("id").asText();
                    if (!cpvId.isBlank()) {
                        cpvCodes.add(cpvId);
                    }
                }
            }
        }

        ArrayNode cpvArray = mapper.createArrayNode();
        cpvCodes.forEach(cpvArray::add);

        result.set("cpv_codes", cpvArray);

        return result;
    }

    private void addUniqueText(Set<String> seen, StringBuilder builder, String text) {
        if (text == null) return;

        String cleaned = text
                .replaceAll("\\s+", " ")
                .trim();

        if (!cleaned.isEmpty() && seen.add(cleaned.toLowerCase())) {
            builder.append(cleaned).append("\n");
        }
    }

    private void safeBatchInsert(List<Document> docs) {

        try {
            vectorStore.add(docs);
            insertedDocuments += docs.size();
        } catch (Exception e) {

            logger.warn("Batch failed, falling back to single document processing");

            for (Document doc : docs) {
                try {
                    vectorStore.add(List.of(doc));
                    insertedDocuments++;
                } catch (Exception ex) {
                    skippedDocuments++;
                    String title = (String) doc.getMetadata().getOrDefault("title", "unknown");
                    logger.error("Skipping document because it exceeds token limit. Title={}", title);
                }
            }
        }
    }

    private Document toDocument(ContractRequestDto dto) {
        List<String> supplierNames = new ArrayList<>();
        List<String> supplierStreets = new ArrayList<>();
        List<String> supplierPostalCodes = new ArrayList<>();
        List<String> supplierCountries = new ArrayList<>();

        if (dto.getSuppliers() != null) {
            for (ContractRequestDto.Supplier s : dto.getSuppliers()) {
                if (s.getName() != null)
                    supplierNames.add(s.getName());

                if (s.getAddress() != null) {

                    if (s.getAddress().getStreet() != null)
                        supplierStreets.add(s.getAddress().getStreet());

                    if (s.getAddress().getPostalCode() != null)
                        supplierPostalCodes.add(s.getAddress().getPostalCode());

                    if (s.getAddress().getCountryName() != null)
                        supplierCountries.add(s.getAddress().getCountryName());
                }
            }
        }

        Map<String, Object> metadata = new HashMap<>();

        if (dto.getBuyer() != null)
            metadata.put("buyer", dto.getBuyer());

        if (dto.getCpvCodes() != null && !dto.getCpvCodes().isEmpty()) {
            metadata.put("cpv_codes", dto.getCpvCodes());

            List<String> cpvPrefixes = dto.getCpvCodes()
                    .stream()
                    .map(code -> code.substring(0, 4))
                    .distinct()
                    .toList();

            metadata.put("cpv_prefixes", cpvPrefixes);
        }

        metadata.put("supplier_names", supplierNames);
        metadata.put("supplier_streets", supplierStreets);
        metadata.put("supplier_postal_codes", supplierPostalCodes);
        metadata.put("supplier_countries", supplierCountries);

        String text = dto.getEmbeddingText() != null ? dto.getEmbeddingText() : "";

        return new Document(text, metadata);
    }
}
