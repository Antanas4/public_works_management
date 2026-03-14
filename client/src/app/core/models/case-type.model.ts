export interface CaseSubtype {
    label: string;
    value: string;
}

export interface CaseType {
    label: string;
    value: string;
    subtypes: CaseSubtype[];
}