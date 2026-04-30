import {Component, Inject, Input, OnInit} from '@angular/core';
import {ProcessingAction} from "../../../../core/models/processing-action.model";
import {ActionStatus} from "../../../../core/enums/action-status.enum";
import {DatePipe, KeyValuePipe, NgClass} from "@angular/common";
import {getProcessingActionStatusLabel} from "../../../../core/utils/case.utils";

@Component({
    selector: 'app-processing-history',
    templateUrl: './processing-history.component.html',
    styleUrls: ['./processing-history.component.scss'],
    imports: [
        KeyValuePipe,
        NgClass,
        DatePipe
    ]
})
export class ProcessingHistoryComponent {
    @Input() processingActions: ProcessingAction[] = [];
    @Input() processingHistoryNotFound = true;

    constructor() {}

    getParameterLabel(parameterKey: string): string {
        const labels: Record<string, string> = {
            address: "Adresas",
            latitude: "Platuma",
            longitude: "Ilguma",
            date: "Data",
            supplierName: "Tiekėjas",
            actionType: "Veiksmo tipas",
            commentId: "Komentaro ID",
            createdAt: "Data"
        };

        return labels[parameterKey] ?? parameterKey;
    }

    protected readonly getProcessingActionStatusLabel = getProcessingActionStatusLabel;
    protected readonly ActionStatus = ActionStatus;
}
