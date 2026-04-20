import {CASE_TYPES} from "../constants/case-types.constant";


export function getSubtypeLabel(subtypeValue: string): string {
  for (const type of CASE_TYPES) {
    const subtype = type.subtypes.find(s => s.value === subtypeValue);
    if (subtype) {
      return subtype.label;
    }
  }

  return subtypeValue;
}