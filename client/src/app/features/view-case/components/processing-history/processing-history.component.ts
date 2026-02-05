import {Component, Inject, Input} from '@angular/core';
import {ProcessingAction} from "../../../../core/models/processing-action.model";
import {I18NEXT_SERVICE, ITranslationService} from "angular-i18next";
import {ActionStatus} from "../../../../core/enums/action-status.enum";

@Component({
    selector: 'app-processing-history',
    templateUrl: './processing-history.component.html',
    styleUrls: ['./processing-history.component.scss']
})
export class ProcessingHistoryComponent {
    @Input() processingActions: ProcessingAction[] = [];
    @Input() processingHistoryNotFound: boolean = true;

    constructor(
        @Inject(I18NEXT_SERVICE) private _i18next: ITranslationService
    ) {}

    getFriendlyActionName(actionType: string): string {
        return this._i18next.t(`processingActions.${actionType}`);
    }

    protected readonly ActionStatus = ActionStatus;
}
