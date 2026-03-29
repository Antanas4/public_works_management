export interface Supplier {
  id?: number;
  name?: string;
  reason?: string;
  confidence?: number;
  handledCaseSubtypes?: string[];
  metadata?: Record<string, string>;
  source?: string;
}