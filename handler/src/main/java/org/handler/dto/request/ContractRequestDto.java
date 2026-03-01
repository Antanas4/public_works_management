package org.handler.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ContractRequestDto {
    @JsonProperty("embedding_text")
    private String embeddingText;

    private String buyer;

    private List<Supplier> suppliers;

    @JsonProperty("cpv_codes")
    private List<String> cpvCodes;

    @Data
    public static class Supplier {
        private String name;
        private Address address;
    }

    @Data
    public static class Address {
        private String street;
        private String postalCode;
        private String countryName;
    }
}
