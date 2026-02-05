import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {REQUESTED_SERVICES} from "../../../../core/constants/requested-services.constant";
import {INCIDENT_CATEGORIES} from "../../../../core/constants/incident-categories.constant";
import {FEEDBACK_CATEGORIES} from "../../../../core/constants/feedback-categories.constant";
import {CaseType} from "../../../../core/enums/case-types.enum";
import {CaseService} from "../../../../core/services/case/case.service";
import {CONDITIONAL_FIELD_VALIDATOR} from "../../utils/conditional-field-validator";
import {clearFieldValidators, clearFieldValues} from '../../../../core/utils/form.utils';
import {ToastService} from "../../../../core/services/toast/toast.service";
import {ToastType} from "../../../../core/enums/toast-type.enum";
import { I18NEXT_SERVICE, ITranslationService } from 'angular-i18next';


@Component({
    selector: 'app-case-form-reactive',
    templateUrl: './case-form-reactive.component.html',
    styleUrls: ['./case-form-reactive.component.scss']
})
export class CaseFormReactiveComponent implements OnInit {
    caseForm!: FormGroup;
    caseType: { label: string; value: string }[] = [];
    requestedServices: { label: string; value: string }[] = [];
    incidentCategories: { label: string; value: string }[] = [];
    feedbackCategories: { label: string; value: string }[] = [];
    maxDate: string;
    ratings: number[] = [1, 2, 3, 4, 5];

    constructor(
        private _formBuilder: FormBuilder,
        private _caseService: CaseService,
        private _toastService: ToastService,
        @Inject(I18NEXT_SERVICE) private _i18next: ITranslationService) {
    }

    ngOnInit(): void {
        this.maxDate = new Date().toISOString().split('T')[0];
        this.initializeSelectOptions();
        this.buildForm();
        this.subscribeToCaseTypeChanges();
    }

    onSubmit(): void {
        if (this.caseForm.invalid) {
            return;
        }

        const casePayload = this.buildCasePayload();

        this._caseService.createCase(casePayload).subscribe({
            next: (): void => {
                this._toastService.show('Case created successfully!', ToastType.Success);
                this.caseForm.reset();
            },
            error: (): void => {
                this._toastService.show('Error while creating case', ToastType.Error);
            }
        });
    }

    private subscribeToCaseTypeChanges(): void {
        this.caseForm.get('type').valueChanges.subscribe(caseType => {
            clearFieldValues(this.caseForm, ['title', 'type']);
            clearFieldValidators(this.caseForm, ['title', 'type']);
            this.updateFieldValidators(caseType);
        });
    }

    private initializeSelectOptions(): void {
        this._i18next.events.languageChanged.subscribe((): void => {
            this.requestedServices = REQUESTED_SERVICES.map(key => ({
                label: this._i18next.t(key),
                value: this._i18next.t(key),
            }));
            this.incidentCategories = INCIDENT_CATEGORIES.map(category => ({
                label: this._i18next.t(category),
                value: this._i18next.t(category)
            }));
            this.feedbackCategories = FEEDBACK_CATEGORIES.map(category => ({
                label: this._i18next.t(category),
                value: this._i18next.t(category)
            }));
            this.caseType = Object.entries(CaseType).map(([ value]) => ({
                label: this._i18next.t(`caseTypes.${value}`),
                value: value
            }));
        });
    }

    private buildForm(): void {
        this.caseForm = this._formBuilder.group({
            title: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(50)]],
            type: ['', Validators.required],
            date: [''],
            requestedService: [''],
            incidentCategory: [''],
            feedbackCategory: [''],
            rating: [''],
            address: [''],
            description: ['']
        });
    }

    private updateFieldValidators(type: string): void {
        const conditionalFieldValidations = CONDITIONAL_FIELD_VALIDATOR[type];

        if (conditionalFieldValidations) {
            Object.entries(conditionalFieldValidations).forEach(([field, validators]) => {
                const control = this.caseForm.get(field);
                if (control) {
                    control.setValidators(validators);
                    control.updateValueAndValidity();
                }
            });
        }
    }

    private buildCasePayload(): any {
        const { title, type, ...optionalFormValues } = this.caseForm.value;

        const parameters = Object.entries(optionalFormValues).reduce((acc, [key, value]) => {
            if (value !== null && value !== undefined && value !== '') {
                acc[key] = value;
            }
            return acc;
        }, {} as { [key: string]: any });

        return {
            title,
            type,
            parameters: parameters
        };
    }
}
