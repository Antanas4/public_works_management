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
  cases: Case[] = [];

  constructor(
    private _caseService: CaseService,
    private _router: Router,
  ) {}

  ngOnInit(): void {
    this._caseService.getAllCases({
      page: 0,
      size: 10
    }).subscribe(res => {
      this.cases = res.items;
      this.casesReady = true;
      this.tryRenderMarkers();
    });
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

  goToCase(id: number): void {
    this._router.navigate(['/cases', id]);
  }

}