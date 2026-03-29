import {ProcessingAction} from "./processing-action.model";
import {Supplier} from "./supplier.model";

export interface Case {
    id?: number;
    type: string;
    subtype: string;
    createdAt?: Date;
    modifiedAt?: Date;
    userId?: number;
    status?: string;
    processingActions?: ProcessingAction[];
    parameters?: Record<string, string>;
    title?: string;
    supplier?: Supplier;
}