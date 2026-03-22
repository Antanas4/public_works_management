package org.handler.service.impl;

import org.handler.service.CpvService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CpvServiceImpl implements CpvService {

    private static final Map<String, String> SUBTYPE_TO_CPV = Map.of(
            "WASTE_MANAGEMENT", "90510000",
            "GRAVEL_ROAD", "45233000",
            "LIGHTING_FAILURES", "50230000"
    );

    @Override
    public String getCpvBySubtype(String subtype) {
        String cpv = SUBTYPE_TO_CPV.get(subtype);

        if (cpv == null) {
            throw new IllegalArgumentException("Unsupported subtype: " + subtype);
        }

        return cpv;
    }
}
