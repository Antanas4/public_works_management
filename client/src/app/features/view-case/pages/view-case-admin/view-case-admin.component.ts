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
    this.getCaseData();
    this.getAllSuppliers();
  }

  getCaseData(): void {
    this._caseService.getCaseById(this.caseId).subscribe({
      next: (caseData: Case): void => {
        this.case = caseData;
        this.processingHistoryNotFound = !caseData.processingActions?.length;
        if (!this.selectedSupplier && caseData.supplier) {
          this.selectedSupplier = caseData.supplier;
        }
      },
      error: (): void => {
        this.processingHistoryNotFound = true;
      }
    });
  }

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

  isSelectedSupplierCurrent(): boolean {
    if (!this.case?.supplier || !this.selectedSupplier) {
      return false;
    }

    return this.case.supplier.id === this.selectedSupplier.id;
  }

  assignSupplier(): void {
    if (!this.selectedSupplier) return;

    const supplier: Supplier = {
      id: this.selectedSupplier.id ?? null,
      name: this.selectedSupplier.name,
      source: this.selectedSupplier.id ? 'MANUAL' : 'AI',
      handledCaseSubtypes: this.selectedSupplier.handledCaseSubtypes ?? [],
      metadata: {
        ...(this.selectedSupplier.metadata ?? {}),
        reason: this.selectedSupplier.reason ?? '',
        confidence:
          this.selectedSupplier.confidence?.toString() ?? ''
      }
    };
    this.isAssigningSupplier = true;

    this._supplierService
      .assignSupplierToCase(this.caseId, supplier)
      .subscribe({
        next: () => {
          this.getCaseData();
          this.getAllSuppliers();
          this.isAssigningSupplier = false;
        },
        error: (err) => {
          console.error('Assign supplier failed:', err);
          this.isAssigningSupplier = false;
        }
      });

  }
}
