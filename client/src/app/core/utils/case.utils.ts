import {CASE_TYPES} from "../constants/case-types.constant";

const CASE_STATUS_LABELS: Record<string, string> = {
  OPEN: "Atviras",
  READY_FOR_REVIEW: "Paruoštas peržiūrai",
  WAITING_FOR_USER_RESPONSE: "Laukiama jūsų atsakymo",
  IN_PROCESSING: "Nagrinėjamas",
  CLOSED: "Uždarytas",
  FAILED: "Neišspręstas"
};

export function getSubtypeLabel(subtypeValue: string): string {
  for (const type of CASE_TYPES) {
    const subtype = type.subtypes.find(s => s.value === subtypeValue);
    if (subtype) {
      return subtype.label;
    }
  }

  return subtypeValue;
}

export function getCaseTypeLabel(typeValue: string): string {
  const type = CASE_TYPES.find((item) => item.value === typeValue);
  return type?.label ?? formatFallbackLabel(typeValue);
}

export function getCaseStatusLabel(statusValue: string): string {
  return CASE_STATUS_LABELS[statusValue] ?? formatFallbackLabel(statusValue);
}

export function getProcessingActionStatusLabel(statusValue: string): string {
  const actionLabels: Record<string, string> = {
    DATA_PROVIDED: "Duomenys pateikti",
    IN_PROGRESS: "Vykdoma"
  };

  return actionLabels[statusValue] ?? formatFallbackLabel(statusValue);
}

function formatFallbackLabel(value: string): string {
  if (!value) {
    return "";
  }

  return value
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
}
