import {Component, Inject, Input, OnInit} from '@angular/core';
import {ProcessingAction} from "../../../../core/models/processing-action.model";
import {ActionStatus} from "../../../../core/enums/action-status.enum";
import {DatePipe, KeyValuePipe, NgClass} from "@angular/common";
import {getProcessingActionStatusLabel} from "../../../../core/utils/case.utils";
import {SharedModule} from "../../../../shared/shared.module";

@Component({
    selector: 'app-processing-history',
    templateUrl: './processing-history.component.html',
    styleUrls: ['./processing-history.component.scss'],
    imports: [
        KeyValuePipe,
        NgClass,
        DatePipe,
        SharedModule
    ]
})
export class ProcessingHistoryComponent {
    @Input() processingActions: ProcessingAction[] = [];
    @Input() processingHistoryNotFound = true;

    protected readonly getProcessingActionStatusLabel = getProcessingActionStatusLabel;
    protected readonly ActionStatus = ActionStatus;
    readonly hiddenParameters = new Set([
        'description',
        'userId',
        'commentId',
        'latitude',
        'longitude'
    ]);

    constructor() {}

    getParameterLabel(parameterKey: string): string {
        const labels: Record<string, string> = {
            address: "Adresas",
            date: "Data",
            supplierName: "Rangovas",
            status: "Būsena",
            actionType: "Veiksmo tipas",
            createdAt: "Data"
        };

        return labels[parameterKey] ?? parameterKey;
    }

    isVisibleParameter(key: string): boolean {
        return !this.hiddenParameters.has(key);
    }
}
