package org.handler.model.enums;

import lombok.Getter;

@Getter
public enum CaseSubtype {

    // ======================
    // ENVIRONMENT
    // ======================

    WASTE_MANAGEMENT("90510000"),
    CEMETERY_MAINTENANCE("98370000"),
    GREEN_AREA_MAINTENANCE("77310000"),
    PLAYGROUND_MAINTENANCE("37535000"),
    TERRITORY_CLEANING("90600000"),
    SPORT_FIELD_MAINTENANCE("45212200"),

    // ======================
    // SURFACE_REPAIR
    // ======================

    GRAVEL_ROAD("45233000"),
    STAIRCASE("45210000"),
    SIDEWALK_REPAIR("45233253"),
    ROAD_REPAIR("45233141"),
    YARD_SURFACE_REPAIR("45230000"),
    POTHOLES_WELLS_COLLAPSE("45233100"),
    BIKE_PATH_REPAIR("45233162"),
    BRIDGE_TUNNEL_MAINTENANCE("45221000"),

    // ======================
    // MALFUNCTION
    // ======================

    LIGHTING_FAILURES("50230000"),

    // ======================
    // BUILDINGS
    // ======================

    BUILDING_ADMINISTRATION("70300000"),
    BUILDING_MAINTENANCE("50700000"),

    // ======================
    // SEASONAL_ISSUES
    // ======================

    SIDEWALK_CLEANING("90610000"),
    BUS_STOP_CLEANING("90610000"),
    YARD_CLEANING("90610000"),
    BIKE_PATH_CLEANING("90610000"),
    PARKING_LOT_CLEANING("90610000"),
    STREET_CLEANING("90610000"),

    // ======================
    // TRAFFIC
    // ======================

    STREET_LIGHTING("50230000"),
    TRAFFIC_SIGNS_AND_MARKING("45233290"),
    TRAFFIC_LIGHTS_FAILURE("50232200");

    private final String cpvCode;

    CaseSubtype(String cpvCode) {
        this.cpvCode = cpvCode;
    }
}