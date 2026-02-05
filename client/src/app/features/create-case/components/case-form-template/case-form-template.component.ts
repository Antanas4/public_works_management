import {Component, OnInit} from '@angular/core';
import {Case} from "../../../../core/models/case.model";
import {CaseService} from "../../../../core/services/case/case.service";
import {ProcessingAction} from "../../../../core/models/processing-action.model";
import {REQUESTED_SERVICES} from "../../../../core/constants/requested-services.constant";
import {INCIDENT_CATEGORIES} from "../../../../core/constants/incident-categories.constant";
import {FEEDBACK_CATEGORIES} from "../../../../core/constants/feedback-categories.constant";
import {CaseType} from "../../../../core/enums/case-types.enum";
import {ToastService} from "../../../../core/services/toast/toast.service";
import {ToastType} from "../../../../core/enums/toast-type.enum";

@Component({
    selector: 'app-case-form-template',
    templateUrl: './case-form-template.component.html',
    styleUrls: ['./case-form-template.component.scss']
})
export class CaseFormTemplateComponent implements OnInit {
    case: Case = {
        title: '',
        type: '',
        processingActions: []
    };
    caseTypes: string[] = [];
    parameters: { [key: string]: string } = {};
    requestedServices: string[] = [];
    incidentCategories: string[] = [];
    feedbackCategories: string[] = [];
    today: string;
    ratings: number[] = [1, 2, 3, 4, 5];

    constructor(private _caseService: CaseService, private _toastService: ToastService) {
    }

    ngOnInit(): void {
      const now = new Date();
      this.today = now.toISOString().split('T')[0]
      this.requestedServices = Object.values(REQUESTED_SERVICES);
      this.incidentCategories = Object.values(INCIDENT_CATEGORIES);
      this.feedbackCategories = Object.values(FEEDBACK_CATEGORIES);
      this.caseTypes = Object.values(CaseType);
    }

    onSubmit(form): void {
        if (!form.valid) return;

        const processingAction: ProcessingAction = {
            parameters: this.parameters,
        };

        this.case.processingActions = [processingAction];

        this._caseService.createCase(this.case).subscribe({
            next: (): void => {
                this._toastService.show('Case created successfully', ToastType.Success);
                form.resetForm();
            },
            error: (err) => {
                console.error('Failed to create case:', err.error, this.case);
            }
        });
    }
}
