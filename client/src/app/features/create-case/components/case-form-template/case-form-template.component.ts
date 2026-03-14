import {AfterViewInit, Component, OnDestroy, OnInit} from '@angular/core';
import {Case} from "../../../../core/models/case.model";
import {CaseService} from "../../../../core/services/case/case.service";
import {ToastService} from "../../../../core/services/toast/toast.service";
import {ToastType} from "../../../../core/enums/toast-type.enum";
import {Router} from "@angular/router";
import * as L from 'leaflet';
import {GeocodingService} from "../../../../core/services/geocoding/geocoding.service";
import {Subject, Subscription} from "rxjs";
import {debounceTime, distinctUntilChanged, filter, switchMap} from "rxjs/operators";

delete (L.Icon.Default.prototype as any)._getIconUrl;

L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'assets/leaflet/marker-icon-2x.png',
  iconUrl: 'assets/leaflet/marker-icon.png',
  shadowUrl: 'assets/leaflet/marker-shadow.png'
});

@Component({
  selector: 'app-case-form-template',
  templateUrl: './case-form-template.component.html',
  styleUrls: ['./case-form-template.component.scss'],
  standalone: false
})
export class CaseFormTemplateComponent implements OnInit, AfterViewInit, OnDestroy {
  private map!: L.Map;
  private marker?: L.Marker;
  private addressInput$ = new Subject<string>();
  private addressSubscription?: Subscription;
  private suppressAddressInputChanges = false;

  case: Case = {
    title: '',
    type: '',
    parameters: null
  };
  parameters: { [key: string]: string } = {};
  today: Date;
  selectedPhotos: File[] = [];
  photoPreviews: string[] = [];

  constructor(
    private _caseService: CaseService,
    private _toastService: ToastService,
    private router: Router,
    private geocodingService: GeocodingService
  ) {
  }

  ngOnInit(): void {
    const state = history.state;

    if (!state?.type || !state?.subtype) {
      this.router.navigate(['/cases/select-type']);
      return;
    }

    this.today = new Date();

    if (state) {
      this.case.type = state.type;
      this.parameters.subtype = state.subtype;
    }

    this.addressSubscription = this.addressInput$
      .pipe(
        filter((address) => !!address && address.trim().length >= 5),
        debounceTime(600),
        distinctUntilChanged(),
        switchMap((address) => this.geocodingService.forwardGeocode(address))
      )
      .subscribe((coords) => {
        if (coords) {
          this.setMarker(coords.lat, coords.lon, false);
        }
      });
  }

  ngOnDestroy(): void {
    this.addressSubscription?.unsubscribe();
  }

  ngAfterViewInit(): void {
    this.map = L.map('map').setView([54.6872, 25.2797], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);

    this.map.on('click', (e: L.LeafletMouseEvent) => {
      const { lat, lng } = e.latlng;
      this.setMarker(lat, lng, true);
    });

    navigator.geolocation.getCurrentPosition(pos => {
      const lat = pos.coords.latitude;
      const lng = pos.coords.longitude;

      this.map.setView([lat, lng], 16);
    });
  }

  onAddressInputChange(value: string): void {
    if (this.suppressAddressInputChanges) {
      return;
    }

    this.parameters.address = value;
    this.addressInput$.next(value);
  }

  private setMarker(lat: number, lng: number, updateAddress: boolean): void {
    if (this.marker) {
      this.marker.setLatLng([lat, lng]);
    } else {
      this.marker = L.marker([lat, lng]).addTo(this.map);
    }

    this.map.setView([lat, lng], 16);
    this.parameters.latitude = lat.toString();
    this.parameters.longitude = lng.toString();

    if (!updateAddress) {
      return;
    }

    this.geocodingService.reverseGeocode(lat, lng).subscribe((address) => {
      if (!address) {
        return;
      }

      this.suppressAddressInputChanges = true;
      this.parameters.address = address;
      // Restore after microtask so we don't immediately re-trigger forward geocoding
      Promise.resolve().then(() => (this.suppressAddressInputChanges = false));
    });
  }

  onSubmit(form): void {
    if (!form.valid) return;

    this.case.parameters = this.parameters;

    this._caseService.createCase(this.case, this.selectedPhotos).subscribe({
      next: (): void => {
        this._toastService.show('Case created successfully', ToastType.Success);
        form.resetForm();
        this.selectedPhotos = [];
        this.photoPreviews = [];
      },
      error: (err) => {
        console.error('Failed to create case:', err.error, this.case);
      }
    });
  }

  onPhotosSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;

    for (let i = 0; i < input.files.length; i++) {
      const file = input.files[i];
      this.selectedPhotos.push(file);
      const reader = new FileReader();
      reader.onload = () => {
        this.photoPreviews.push(reader.result as string);
      };
      reader.readAsDataURL(file);
    }

  }

  removePhoto(index: number): void {
    this.selectedPhotos.splice(index, 1);
    this.photoPreviews.splice(index, 1);
  }
}
