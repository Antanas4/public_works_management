import {UntypedFormGroup} from '@angular/forms';

export function clearFieldValidators(form: UntypedFormGroup, excludeFields: string[] = []): void {
    if (!(form instanceof UntypedFormGroup)) return;

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

export function clearFieldValues(form: UntypedFormGroup, excludeFields: string[] = []): void {
    if (!(form instanceof UntypedFormGroup)) return;

    for (const field of Object.keys(form.controls)) {
        if (!excludeFields.includes(field)) {
            const control = form.get(field);
            if (control) {
                control.reset('', {onlySelf: true, emitEvent: false});
            }
        }
    }
}
