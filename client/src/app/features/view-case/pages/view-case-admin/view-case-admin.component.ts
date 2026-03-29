import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {CaseService} from "../../../../core/services/case/case.service";
import {Case} from "../../../../core/models/case.model";
import {Supplier} from "../../../../core/models/supplier.model";
import {SupplierService} from "../../../../core/services/supplier/supplier.service";

@Component({
  selector: 'app-view-case-admin',
  templateUrl: './view-case-admin.component.html',
  styleUrls: ['./view-case-admin.component.scss'],
  standalone: false
})
export class ViewCaseAdminComponent implements OnInit {
  caseId!: number;
  case!: Case;
  processingHistoryNotFound = true;
  isTypeEditMode: boolean = false;
  selectedType?: string;
  caseTypeOptions: Array<{ label: string, value: string }> = [];
  suggestedSuppliers: Supplier[] = [];
  allSuppliers: Supplier[] = [];
  selectedSupplier?: Supplier;
  isLoadingSuggestions: boolean = false;
  isAssigningSupplier: boolean = false;

  constructor(
    private readonly _route: ActivatedRoute,
    private readonly _caseService: CaseService,
    private readonly _supplierService: SupplierService
  ) {
  }

  ngOnInit(): void {
    this.caseId = Number(this._route.snapshot.paramMap.get('id'));
    // this.caseTypeOptions = this.buildCaseTypeOptions();
    this.getCaseData();
    this.getAllSuppliers();
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

  loadSupplierSuggestions(): void {
    this.isLoadingSuggestions = true;

    this._caseService
      .suggestSuppliersForCase(this.caseId)
      .subscribe({
        next: suppliers => {
          this.suggestedSuppliers = suppliers;
          this.isLoadingSuggestions = false;
        },
        error: () => {
          this.isLoadingSuggestions = false;
        }
      });
  }

  getAllSuppliers(): void {
    this._supplierService
      .getAllSuppliers()
      .subscribe({
        next: suppliers => {
          this.allSuppliers = suppliers;
        }
      });
  }

  assignSupplier(): void {
    if (!this.selectedSupplier) return;
    const isAiSupplier = !this.selectedSupplier.id;

    const payload: Supplier = {
      ...this.selectedSupplier,
      metadata: {
        ...(this.selectedSupplier.metadata ?? {}),
        reason: this.selectedSupplier.reason ?? '',
        confidence: this.selectedSupplier.confidence ?? ''
      },

      source: isAiSupplier ? 'AI' : 'MANUAL'
    };
    this.isAssigningSupplier = true;

    this._supplierService
      .assignSupplierToCase(this.caseId, payload)
      .subscribe({
        next: () => {
          this.getCaseData();
          this.isAssigningSupplier = false;
        },
        error: () => {
          this.isAssigningSupplier = false;
        }
      });
  }
}
