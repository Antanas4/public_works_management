import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {CaseService} from "../../../../core/services/case/case.service";
import {Case} from "../../../../core/models/case.model";
import {Supplier} from "../../../../core/models/supplier.model";
import {SupplierService} from "../../../../core/services/supplier/supplier.service";
import {getCaseStatusLabel, getCaseTypeLabel, getSubtypeLabel} from "../../../../core/utils/case.utils";

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
  supplierSearchTerm: string = '';
  selectedStatus?: string;
  statusOptions = [
    {value: 'IN_PROCESSING', label: 'Nagrinėjamas'},
    {value: 'READY_FOR_REVIEW', label: 'Paruoštas peržiūrai'},
    {value: 'IN_SUPPLIER_PROCESSING', label: 'Perduota rangovui'},
    {value: 'CLOSED', label: 'Uždaryta'},
    {value: 'FAILED', label: 'Nepavyko išspręsti'}
  ];

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
        this.selectedStatus = caseData.status;
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
          this.suggestedSuppliers = suppliers.sort((a, b) => b.confidence - a.confidence);
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

    const normalizedSelected = this.normalizeName(this.selectedSupplier.name);
    const existingSupplier = this.allSuppliers.find(s => this.normalizeName(s.name) === normalizedSelected);

    if (existingSupplier) {
      this.assignExistingSupplier(existingSupplier.id!);
      return;
    }

    this.isAssigningSupplier = true;
    const supplierRequest = this.buildSupplier();

    this._supplierService
      .createSupplier(supplierRequest)
      .subscribe({
        next: supplier => {
          this.assignExistingSupplier(supplier.id!);
        },
        error: err => {
          if (err.status === 409 && err.error?.supplier) {
            this.assignExistingSupplier(err.error.supplier.id);
            return;
          }
          console.error(
            'Nepavyko sukurti tiekėjo:',
            err
          );
          this.isAssigningSupplier = false;
        }
      });
  }

  filteredSuppliers(): Supplier[] {
    if (!this.supplierSearchTerm.trim()) {
      return this.allSuppliers;
    }
    return this.allSuppliers.filter(s =>
      s.name
        .toLowerCase()
        .includes(this.supplierSearchTerm.toLowerCase())
    );

  }

  formatHandledCaseSubtypes(subtypes?: string[]): string {
    if (!subtypes?.length) {
      return 'Nepateikta veiklos sričių';
    }

    return subtypes
      .map(subtype => getSubtypeLabel(subtype))
      .join(', ');
  }

  updateCaseStatus(): void {
    if (!this.selectedStatus) return;
    this._caseService
      .updateCaseStatus(this.caseId, this.selectedStatus)
      .subscribe({
        next: () => this.getCaseData(),
        error: err => console.error("Nepavyko atnaujinti būsenos:", err)
      });
  }

  isStatusOptionDisabled(status: string): boolean {
    return status === 'IN_SUPPLIER_PROCESSING' && !this.case?.supplier;
  }

  private assignExistingSupplier(supplierId: number) {
    this._caseService.assignSupplierToCase(this.caseId, supplierId)
      .subscribe({
        next: () => {
          this.getCaseData();
          this.getAllSuppliers();
          this.isAssigningSupplier = false;
        },
        error: err => {
          console.error('Nepavyko priskirti tiekėjo:', err);
          this.isAssigningSupplier = false;
        }
      });
  }


  private buildSupplier(): Supplier {
    return {
      id: this.selectedSupplier.id ?? null,
      name: this.selectedSupplier.name,
      source: this.selectedSupplier.id ? 'MANUAL' : 'AI',
      metadata: {
        ...(this.selectedSupplier.metadata ?? {}),
        reason: this.selectedSupplier.reason ?? '',
        confidence:
          this.selectedSupplier.confidence?.toString() ?? ''
      }
    };
  }

  private normalizeName(name: string): string {

    if (!name) return '';

    return name
      .replace(/[^\p{L}0-9\s&()-]/gu, '')
      .trim()
      .replace(/\s+/g, ' ')
  }

  protected readonly getCaseStatusLabel = getCaseStatusLabel;
  protected readonly getCaseTypeLabel = getCaseTypeLabel;
  protected readonly getSubtypeLabel = getSubtypeLabel;
}
