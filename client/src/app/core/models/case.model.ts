import {ProcessingAction} from "./processing-action.model";

export interface Case {
    id?: number;
    type: string;
    createdAt?: Date;
    modifiedAt?: Date;
    userId?: number;
    status?: string,
    processingActions?: ProcessingAction[];
    parameters?: Record<string, string>;
    title?: string;
}