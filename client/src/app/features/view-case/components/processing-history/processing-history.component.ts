import {Component, Inject, Input, OnInit} from '@angular/core';
import {ProcessingAction} from "../../../../core/models/processing-action.model";
import {ActionStatus} from "../../../../core/enums/action-status.enum";
import {SharedModule} from "../../../../shared/shared.module";
import {DatePipe, KeyValuePipe, NgClass} from "@angular/common";

@Component({
    selector: 'app-processing-history',
    templateUrl: './processing-history.component.html',
    styleUrls: ['./processing-history.component.scss'],
})
export class ProcessingHistoryComponent {
    @Input() processingActions: ProcessingAction[] = [];
    @Input() processingHistoryNotFound = true;

    constructor(
    ) {
        console.log("Hi");
    }

    protected readonly ActionStatus = ActionStatus;
}
