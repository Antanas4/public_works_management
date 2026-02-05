export interface ProcessingAction {
    id?: number;
    createdAt?: Date;
    status?: string;
    parameters: Record<string, string>;
    caseId?: number;
}