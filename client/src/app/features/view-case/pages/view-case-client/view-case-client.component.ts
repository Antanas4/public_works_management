import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {CaseService} from "../../../../core/services/case/case.service";
import {Case} from 'src/app/core/models/case.model';

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

    constructor(
        private readonly _route: ActivatedRoute,
        private readonly _caseService: CaseService,
    ) {
    }

    ngOnInit(): void {
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
}
