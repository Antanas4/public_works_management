package org.handler.service.impl;

import org.handler.service.CpvService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CpvServiceImpl implements CpvService {

    private static final Map<String, String> SUBTYPE_TO_CPV = Map.ofEntries(

            //ENVIRONMENT
            Map.entry("WASTE_MANAGEMENT", "90510000"),
            Map.entry("CEMETERY_MAINTENANCE", "98370000"),
            Map.entry("GREEN_AREA_MAINTENANCE", "77310000"),
            Map.entry("PLAYGROUND_MAINTENANCE", "37535000"),
            Map.entry("TERRITORY_CLEANING", "90600000"),
            Map.entry("SPORT_FIELD_MAINTENANCE", "45212200"),

            //SURFACE_REPAIR
            Map.entry("GRAVEL_ROAD", "45233000"),
            Map.entry("STAIRCASE", "45210000"),
            Map.entry("SIDEWALK_REPAIR", "45233253"),
            Map.entry("ROAD_REPAIR", "45233141"),
            Map.entry("YARD_SURFACE_REPAIR", "45230000"),
            Map.entry("POTHOLES_WELLS_COLLAPSE", "45233100"),
            Map.entry("BIKE_PATH_REPAIR", "45233162"),
            Map.entry("BRIDGE_TUNNEL_MAINTENANCE", "45221000"),

            //MALFUNCTION
            Map.entry("LIGHTING_FAILURES", "50230000"),

            // ======================
            // BUILDINGS
            // ======================

            Map.entry("BUILDING_ADMINISTRATION", "70300000"),
            Map.entry("BUILDING_MAINTENANCE", "50700000"),

            // ======================
            // SEASONAL_ISSUES
            // ======================

            Map.entry("SIDEWALK_CLEANING", "90610000"),
            Map.entry("BUS_STOP_CLEANING", "90610000"),
            Map.entry("YARD_CLEANING", "90610000"),
            Map.entry("BIKE_PATH_CLEANING", "90610000"),
            Map.entry("PARKING_LOT_CLEANING", "90610000"),
            Map.entry("STREET_CLEANING", "90610000"),

            // ======================
            // TRAFFIC (EISMAS)
            // ======================

            Map.entry("STREET_LIGHTING", "50230000"),
            Map.entry("TRAFFIC_SIGNS_AND_MARKING", "45233290"),
            Map.entry("TRAFFIC_LIGHTS_FAILURE", "50232200")
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
