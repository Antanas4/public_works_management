import {CaseType} from "../models/case-type.model";

export const CASE_TYPES: CaseType[] = [
    {
        label: "Aplinkos tvarkymas",
        value: "ENVIRONMENT",
        subtypes: [
            { label: "Kapinių priežiūra", value: "CEMETERY_MAINTENANCE" },
            { label: "Komunalinių atliekų tvarkymas, konteineriai", value: "WASTE_MANAGEMENT" },
            { label: "Želdynų priežiūra", value: "GREEN_AREA_MAINTENANCE" },
            { label: "Vaikų žaidimo aikštelės", value: "PLAYGROUND_MAINTENANCE" },
            { label: "Teritorijų tvarkymas", value: "TERRITORY_CLEANING" },
            { label: "Sporto aikštelės, treniruokliai", value: "SPORT_FIELD_MAINTENANCE" },
        ]
    },
    {
        label: "Dangų remontas",
        value: "SURFACE_REPAIR",
        subtypes: [
            { label: "Žvyrkeliai", value: "GRAVEL_ROAD" },
            { label: "Laiptai", value: "STAIRCASE" },
            { label: "Šaligatvių remontas", value: "SIDEWALK_REPAIR" },
            { label: "Gatvių remontas", value: "ROAD_REPAIR" },
            { label: "Kiemų dangos remontas", value: "YARD_SURFACE_REPAIR" },
            { label: "Duobės, įgriuvos, šuliniai", value: "POTHOLES_WELLS_COLLAPSE" },
            { label: "Dviračių takų problemos", value: "BIKE_PATH_REPAIR" },
            { label: "Tiltų, viadukų, tunelių ir estakadų priežiūra", value: "BRIDGE_TUNNEL_MAINTENANCE" }
        ]
    },
    {
        label: "Gedimai",
        value: "MALFUNCTION",
        subtypes: [
            { label: "Apšvietimo gedimai", value: "LIGHTING_FAILURES" },
        ]
    },
    {
        label: "Pastatai",
        value: "BUILDINGS",
        subtypes: [
            { label: "Pastatų administravimas", value: "BUILDING_ADMINISTRATION" },
            { label: "Statinių priežiūra", value: "BUILDING_MAINTENANCE" }
        ]
    },
    {
        label: "Sezoninės problemos",
        value: "SEASONAL_ISSUES",
        subtypes: [
            { label: "Šaligatvių valymas", value: "SIDEWALK_CLEANING" },
            { label: "Stotelių valymas", value: "BUS_STOP_CLEANING" },
            { label: "Daugiabučių namų kiemų valymas", value: "YARD_CLEANING" },
            { label: "Dviračių takų valymas", value: "BIKE_PATH_CLEANING" },
            { label: "Automobilių stovėjimo aikštelių valymas", value: "PARKING_LOT_CLEANING" },
            { label: "Gatvių valymas", value: "STREET_CLEANING" }
        ]
    },
    {
        label: "Eismas",
        value: "TRAFFIC",
        subtypes: [
            { label: "Gatvių apšvietimas", value: "STREET_LIGHTING" },
            { label: "Ženklai, horizontalus ženklinimas, greičio mažinimo kalneliai ir kitos eismo organizavimo priemonės", value: "TRAFFIC_SIGNS_AND_MARKING" },
            { label: "Viešasis transportas", value: "PUBLIC_TRANSPORT_INFRASTRUCTURE" },
            { label: "Šviesoforai (gedimai, sutrikimai)", value: "TRAFFIC_LIGHTS_FAILURE" }
        ]
    }
];