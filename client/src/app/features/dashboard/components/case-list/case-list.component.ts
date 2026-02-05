import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {Case} from "../../../../core/models/case.model";
import {CaseService} from "../../../../core/services/case/case.service";
import {ActivatedRoute, Router} from "@angular/router";
import {CasePaginationRequest} from "../../../../core/models/pagination-request.model";
import {PaginationResponse} from "../../../../core/models/pagination-response.model";
import {CaseType} from "../../../../core/enums/case-types.enum";
import {CaseStatus} from "../../../../core/enums/case-statuses.enum";

@Component({
    selector: 'app-case-list',
    templateUrl: './case-list.component.html',
    styleUrls: ['./case-list.component.scss']
})
export class CaseListComponent implements OnInit {
    @Output() totalElementsChange:EventEmitter<number> = new EventEmitter<number>();

    cases: Case[] = [];
    loading: boolean = true;
    error: string | null = null;
    paginationResponse: PaginationResponse<Case>;
    caseStatuses = Object.values(CaseStatus);
    caseTypes = Object.values(CaseType);
    selectedSortOption: string = 'createdAt,DESC';
    casePaginationRequest: CasePaginationRequest = {
        page: '0',
        size: '10',
        sortField: 'id',
        direction: 'DESC',
        status: '',
        type: ''
    };

    constructor(
        private _caseService: CaseService,
        private _router: Router,
        private _route: ActivatedRoute) {}

    ngOnInit(): void {
        this._route.queryParams.subscribe(params => {
            const page = params['page'];
            if (page) {
                this.casePaginationRequest.page = page;
            }
            this.getCases();
        });
    }

    getCases():void {
        this._caseService.getCasesByUserId(1, this.casePaginationRequest).subscribe({
            next: (paginationResponse: PaginationResponse<Case>): void => {
                this.cases = paginationResponse.items;
                this.loading = false;
                this.paginationResponse = paginationResponse;
                this.totalElementsChange.emit(paginationResponse.totalElements);
            },
            error: (): void => {
                this.error = 'Failed to load cases';
                this.loading = false;
            }
        });
    }

    goToCase(id: number):void {
        this._router.navigate(['/cases', id]);
    }

    onPageChange(newPage: number):void {
        this.casePaginationRequest.page = String(newPage);

        this._router.navigate([], {
            relativeTo: this._route,
            queryParams: {page: newPage},
            queryParamsHandling: 'merge'
        });

        this.getCases();
    }

    onFiltersChange() {
        this.casePaginationRequest.page = '0';
        this.getCases();
    }

    onSortOptionChange() {
        const [field, direction] = this.selectedSortOption.split(',');
        this.casePaginationRequest.sortField = field;
        this.casePaginationRequest.direction = direction;
        this.casePaginationRequest.page = '0';
        this.getCases();
    }
}
