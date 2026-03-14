import {CaseType} from "../models/case-type.model";

export const CASE_TYPES: CaseType[] = [
    {
        label: "Aplinkos tvarkymas",
        value: "ENVIRONMENT",
        subtypes: [
            { label: "Komunalinių atliekų tvarkymas", value: "WASTE_MANAGEMENT" },
        ]
    },
    {
        label: "Dangų remontas",
        value: "SURFACE_REPAIR",
        subtypes: [
            { label: "Žvyrkeliai", value: "GRAVEL_ROAD" },
        ]
    },
    {
        label: "Gedimai",
        value: "MALFUNCTION",
        subtypes: [
            { label: "Apšvietimo gedimai", value: "LIGHTING_FAILURES" },
        ]
    }
];