import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {CaseService} from "../../../../core/services/case/case.service";
import {Case} from "../../../../core/models/case.model";

@Component({
  selector: 'app-view-case-admin',
  templateUrl: './view-case-admin.component.html',
  styleUrls: ['./view-case-admin.component.scss'],
  standalone: false
})
export class ViewCaseAdminComponent implements OnInit{
  caseId!: number;
  case!: Case;
  processingHistoryNotFound = true;

  isTypeEditMode = false;
  selectedType?: string;

  caseTypeOptions: Array<{label: string, value: string}> = [];

  constructor(
    private readonly _route: ActivatedRoute,
    private readonly _caseService: CaseService
  ) {
  }

  ngOnInit(): void {
    this.caseId = Number(this._route.snapshot.paramMap.get('id'));
    // this.caseTypeOptions = this.buildCaseTypeOptions();
    this.getCaseData();
  }

  getCaseData(): void {
    this._caseService.getCaseById(this.caseId).subscribe({
      next: (caseData: Case): void => {
        this.case = caseData;
        this.processingHistoryNotFound = !caseData.processingActions?.length;
        if (!this.selectedType) {
          this.selectedType = caseData.type;
        }
        console.log('caseData:', this.case);
      },
      error: (): void => {
        this.processingHistoryNotFound = true;
      }
    });
  }

  startClarifyType(): void {
    this.isTypeEditMode = true;
    this.selectedType = this.case?.type;
  }

  cancelClarifyType(): void {
    this.isTypeEditMode = false;
    this.selectedType = this.case?.type;
  }

  saveCaseType(): void {
    if (!this.case || !this.selectedType) {
      return;
    }

    // For now update UI state; backend update endpoint not currently available in this project.
    this.case.type = this.selectedType;
    this.case.modifiedAt = new Date();
    this.isTypeEditMode = false;
  }

  // private buildCaseTypeOptions(): Array<{label: string, value: string}> {
  //   return CASE_TYPES.reduce((result: Array<{label: string, value: string}>, type: CaseType) => {
  //     result.push({ label: type.label, value: type.value });
  //     if (type.subtypes) {
  //       type.subtypes.forEach(sub => result.push({ label: `  ${sub.label}`, value: sub.value }));
  //     }
  //     return result;
  //   }, []);
  // }
}
