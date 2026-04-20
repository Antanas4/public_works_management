import {
  AfterViewInit,
  Component,
  OnInit
} from '@angular/core';

declare const L: typeof import('leaflet');
import type {} from 'leaflet.markercluster';

import { CaseService } from "../../../../core/services/case/case.service";
import { Case } from "../../../../core/models/case.model";
import {Router} from "@angular/router";
import {
  getCaseStatusLabel,
  getCaseTypeLabel,
  getSubtypeLabel
} from "../../../../core/utils/case.utils";
import {AuthService} from "../../../../core/services/auth/auth.service";
import {CaseStatus} from "../../../../core/enums/case-statuses.enum";
import {CaseType} from "../../../../core/enums/case-types.enum";
import {CasePaginationRequest} from "../../../../core/models/pagination-request.model";
import {PaginationResponse} from "../../../../core/models/pagination-response.model";

@Component({
  selector: 'app-dashboard-client',
  templateUrl: './dashboard-client.component.html',
  styleUrls: ['./dashboard-client.component.scss'],
  standalone: false
})
export class DashboardClientComponent implements OnInit, AfterViewInit {
  private map!: L.Map;
  private markerCluster!: any;
  private mapReady = false;
  private casesReady = false;
  currentUserId: number | null = null;
  cases: Case[] = [];
  loading = true;
  error: string | null = null;
  caseStatuses = Object.values(CaseStatus);
  caseTypes = Object.values(CaseType);
  selectedSortOption = 'createdAt,DESC';
  ownershipFilter: 'ALL' | 'MINE' = 'ALL';
  casePaginationRequest: CasePaginationRequest = {
    page: '0',
    size: '100',
    sortField: 'createdAt',
    direction: 'DESC',
    status: '',
    type: ''
  };

  constructor(
    private _caseService: CaseService,
    private _router: Router,
    private _authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.currentUserId = this._authService.getCurrentUser()?.id ?? null;
    this.getCases();
  }

  ngAfterViewInit(): void {

    this.map = L.map('map').setView(
      [54.6872, 25.2797],
      12
    );

    L.tileLayer(
      'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      {
        attribution: '&copy; OpenStreetMap contributors'
      }
    ).addTo(this.map);

    this.markerCluster = L.markerClusterGroup();

    this.map.addLayer(this.markerCluster);

    this.mapReady = true;

    this.tryRenderMarkers();
  }

  private tryRenderMarkers(): void {

    if (!this.mapReady || !this.casesReady) return;

    this.renderMarkers();

  }

  renderMarkers(): void {
    if (!this.map || !this.markerCluster) return;

    this.markerCluster.clearLayers();

    const markers: L.Marker[] = [];

    this.cases.forEach(caseItem => {

      const dataAction = caseItem.processingActions
        ?.find(a => a.status === 'DATA_PROVIDED');

      const lat = Number(dataAction?.parameters?.latitude);
      const lng = Number(dataAction?.parameters?.longitude);

      if (!Number.isFinite(lat) || !Number.isFinite(lng)) return;

      const marker = L.marker([lat, lng])
        .bindPopup(`
      <strong>${caseItem.title}</strong><br>
      ${dataAction?.parameters?.address ?? ''}
    `);

      marker.on('click', () =>
        this.map.setView([lat, lng], 16)
      );

      markers.push(marker);

    });
    this.markerCluster.addLayers(markers);
  }

  getCases(): void {
    this.loading = true;
    this.error = null;

    const request$ = this.ownershipFilter === 'MINE'
      ? this._caseService.getUserCases(this.casePaginationRequest)
      : this._caseService.getAllCases(this.casePaginationRequest);

    request$.subscribe({
      next: (res: PaginationResponse<Case>): void => {
        this.cases = this.applyClientFilters(res.items);
        this.casesReady = true;
        this.loading = false;
        this.tryRenderMarkers();
      },
      error: (): void => {
        this.error = 'Nepavyko įkelti pranešimų';
        this.loading = false;
      }
    });
  }

  onFiltersChange(): void {
    this.casePaginationRequest.page = '0';
    this.getCases();
  }

  onSortOptionChange(): void {
    const [field, direction] = this.selectedSortOption.split(',');
    this.casePaginationRequest.sortField = field;
    this.casePaginationRequest.direction = direction;
    this.casePaginationRequest.page = '0';
    this.getCases();
  }

  private applyClientFilters(cases: Case[]): Case[] {
    return cases.filter((caseItem: Case) => {
      const statusMatches = !this.casePaginationRequest.status
        || caseItem.status === this.casePaginationRequest.status;
      const typeMatches = !this.casePaginationRequest.type
        || caseItem.type === this.casePaginationRequest.type;

      return statusMatches && typeMatches;
    });
  }

  goToCase(id: number): void {
    this._router.navigate(['/cases', id]);
  }

  isWaitingForCurrentUserResponse(caseItem: Case): boolean {
    return this.currentUserId !== null
      && caseItem.userId === this.currentUserId
      && caseItem.status === CaseStatus.WAITING_FOR_USER_RESPONSE;
  }

  getWaitingForMyResponseCount(): number {
    return this.cases.filter((caseItem) =>
      this.isWaitingForCurrentUserResponse(caseItem)
    ).length;
  }

  protected readonly getSubtypeLabel = getSubtypeLabel;
  protected readonly getCaseTypeLabel = getCaseTypeLabel;
  protected readonly getCaseStatusLabel = getCaseStatusLabel;
}
