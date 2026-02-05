import {FormGroup} from '@angular/forms';

export function clearFieldValidators(form: FormGroup, excludeFields: string[] = []): void {
    if (!(form instanceof FormGroup)) return;

    for (const field of Object.keys(form.controls)) {
        if (!excludeFields.includes(field)) {
            const control = form.get(field);
            if (control) {
                control.clearValidators();
                control.updateValueAndValidity({onlySelf: true, emitEvent: false});
            }
        }
    }
}

export function clearFieldValues(form: FormGroup, excludeFields: string[] = []): void {
    if (!(form instanceof FormGroup)) return;

    for (const field of Object.keys(form.controls)) {
        if (!excludeFields.includes(field)) {
            const control = form.get(field);
            if (control) {
                control.reset('', {onlySelf: true, emitEvent: false});
            }
        }
    }
}
