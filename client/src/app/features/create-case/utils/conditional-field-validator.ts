import {Validators} from "@angular/forms";

export const CONDITIONAL_FIELD_VALIDATOR: Record<string, { [key: string]: any[] }> = {
  INCIDENT: {
    incidentCategory: [Validators.required],
    date: [Validators.required],
    address: [Validators.required, Validators.minLength(5), Validators.maxLength(30)],
    description: [Validators.required, Validators.minLength(10), Validators.maxLength(500)],
  },
  REQUEST: {
    requestedService: [Validators.required],
    address: [Validators.required, Validators.minLength(5), Validators.maxLength(30)],
    description: [Validators.required, Validators.minLength(10), Validators.maxLength(500)],
  },
  FEEDBACK: {
    feedbackCategory: [Validators.required],
    rating: [Validators.required],
    description: [Validators.required, Validators.minLength(10), Validators.maxLength(500)],

  },
  COMPLAINT: {
    date: [Validators.required],
    address: [Validators.required, Validators.minLength(5), Validators.maxLength(30)],
    description: [Validators.required, Validators.minLength(10), Validators.maxLength(500)],
  }
};
