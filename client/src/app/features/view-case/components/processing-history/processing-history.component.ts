import {Component, Inject, Input} from '@angular/core';
import {ProcessingAction} from "../../../../core/models/processing-action.model";
import {ActionStatus} from "../../../../core/enums/action-status.enum";

@Component({
    selector: 'app-processing-history',
    templateUrl: './processing-history.component.html',
    styleUrls: ['./processing-history.component.scss']
})
export class ProcessingHistoryComponent {
    @Input() processingActions: ProcessingAction[] = [];
    @Input() processingHistoryNotFound = true;

    constructor(
    ) {}

    protected readonly ActionStatus = ActionStatus;
}
