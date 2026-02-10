import { Component, DestroyRef, inject } from '@angular/core';
import {
    NonNullableFormBuilder,
    Validators,
} from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { CaseType } from '../../../../core/enums/case-types.enum';
import { CaseService } from '../../../../core/services/case/case.service';
import { CONDITIONAL_FIELD_VALIDATOR } from '../../utils/conditional-field-validator';
import {
    clearFieldValidators,
    clearFieldValues,
} from '../../../../core/utils/form.utils';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { ToastType } from '../../../../core/enums/toast-type.enum';
import {REQUESTED_SERVICES} from "../../../../core/constants/requested-services.constant";
import {INCIDENT_CATEGORIES} from "../../../../core/constants/incident-categories.constant";
import {FEEDBACK_CATEGORIES} from "../../../../core/constants/feedback-categories.constant";

@Component({
    selector: 'app-case-form-reactive',
    templateUrl: './case-form-reactive.component.html',
    styleUrls: ['./case-form-reactive.component.scss'],
})
export class CaseFormReactiveComponent {
    private readonly fb = inject(NonNullableFormBuilder);
    private readonly destroyRef = inject(DestroyRef);

    readonly caseForm = this.fb.group({
        title: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(50)]],
        type: ['', Validators.required],
        date: [''],
        requestedService: [''],
        incidentCategory: [''],
        feedbackCategory: [''],
        rating: [''],
        address: [''],
        description: [''],
    });

    readonly maxDate = new Date().toISOString().split('T')[0];
    readonly ratings = [1, 2, 3, 4, 5];

    readonly caseType = Object.values(CaseType).map(v => ({ label: v, value: v }));
    readonly requestedServices = REQUESTED_SERVICES.map(v => ({ label: v, value: v }));
    readonly incidentCategories = INCIDENT_CATEGORIES.map(v => ({ label: v, value: v }));
    readonly feedbackCategories = FEEDBACK_CATEGORIES.map(v => ({ label: v, value: v }));

    constructor(
        private readonly caseService: CaseService,
        private readonly toastService: ToastService
    ) {
        this.caseForm.controls.type.valueChanges
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(type => {
                clearFieldValues(this.caseForm, ['title', 'type']);
                clearFieldValidators(this.caseForm, ['title', 'type']);
                this.updateFieldValidators(type);
            });
    }

    get title() { return this.caseForm.controls.title; }
    get type() { return this.caseForm.controls.type; }
    get incidentCategory() { return this.caseForm.controls.incidentCategory; }
    get requestedService() { return this.caseForm.controls.requestedService; }
    get feedbackCategory() { return this.caseForm.controls.feedbackCategory; }
    get rating() { return this.caseForm.controls.rating; }
    get address() { return this.caseForm.controls.address; }
    get description() { return this.caseForm.controls.description; }

    onSubmit(): void {
        if (this.caseForm.invalid) return;

        this.caseService.createCase(this.buildCasePayload()).subscribe({
            next: () => {
                this.toastService.show('Case created successfully!', ToastType.Success);
                this.caseForm.reset();
            },
            error: () => {
                this.toastService.show('Error while creating case', ToastType.Error);
            },
        });
    }

    private updateFieldValidators(type: string): void {
        const validators = CONDITIONAL_FIELD_VALIDATOR[type];
        if (!validators) return;

        Object.entries(validators).forEach(([field, rules]) => {
            const control = this.caseForm.controls[field as keyof typeof this.caseForm.controls];
            control.setValidators(rules);
            control.updateValueAndValidity();
        });
    }

    private buildCasePayload() {
        const { title, type, ...rest } = this.caseForm.getRawValue();

        const parameters = Object.fromEntries(
            Object.entries(rest).filter(([, v]) => v !== '' && v != null)
        );

        return { title, type, parameters };
    }
}
