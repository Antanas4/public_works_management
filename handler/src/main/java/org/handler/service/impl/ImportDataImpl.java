package org.handler.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.handler.dto.request.ContractRequestDto;
import org.handler.service.ImportDataService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImportDataImpl implements ImportDataService {
    private final VectorStore vectorStore;
    private static final int BATCH_SIZE = 100;

    @Override
    public void importFile(Path path) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<Document> documents = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;

            while ((line = reader.readLine()) != null) {
                ContractRequestDto dto = mapper.readValue(line, ContractRequestDto.class);
                Document document = toDocument(dto);

                documents.add(document);

                if (documents.size() >= BATCH_SIZE) {
                    vectorStore.add(documents);
                    documents.clear();
                }
            }

            if (!documents.isEmpty()) {
                vectorStore.add(documents);
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

        if (dto.getCpvCodes() != null)
            metadata.put("cpv_codes", dto.getCpvCodes());

        metadata.put("supplier_names", supplierNames);
        metadata.put("supplier_streets", supplierStreets);
        metadata.put("supplier_postal_codes", supplierPostalCodes);
        metadata.put("supplier_countries", supplierCountries);

        String text = dto.getEmbeddingText() != null ? dto.getEmbeddingText() : "";

        return new Document(text, metadata);
    }
}
