export interface Supplier {
  id?: number;
  supplierName?: string;
  reason?: string;
  confidence?: string;
  handledCaseSubtypes?: string[];
  metadata?: Record<string, string>;
  source?: 'AI' | 'MANUAL';
}