import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {CaseService} from "../../../../core/services/case/case.service";
import {Case} from 'src/app/core/models/case.model';
import {AuthService} from "../../../../core/services/auth/auth.service";
import {CaseStatus} from "../../../../core/enums/case-statuses.enum";

@Component({
    selector: 'app-view-case',
    templateUrl: './view-case-client.component.html',
    styleUrls: ['./view-case-client.component.scss'],
    standalone: false
})
export class ViewCaseClientComponent implements OnInit {
    caseId!: number;
    case!: Case;
    processingHistoryNotFound: boolean = true;
    currentUserId: number | null = null;

    constructor(
        private readonly _route: ActivatedRoute,
        private readonly _caseService: CaseService,
        private readonly _authService: AuthService,
    ) {
    }

    ngOnInit(): void {
        this.currentUserId = this._authService.getCurrentUser()?.id ?? null;
        this.caseId = Number(this._route.snapshot.paramMap.get('id'));
        this.getCaseData();
    }

    getCaseData(): void {
        this._caseService.getCaseById(this.caseId).subscribe({
            next: (caseData: Case): void => {
                this.case = caseData;
            },
            error: (): void => {
                this.processingHistoryNotFound = true;
            }
        });
    }

    isCurrentUserCaseOwner(): boolean {
        return this.currentUserId !== null && this.case?.userId === this.currentUserId;
    }

    isWaitingForCurrentUserResponse(): boolean {
        return this.isCurrentUserCaseOwner() && this.case?.status === CaseStatus.WAITING_FOR_USER_RESPONSE;
    }
}
