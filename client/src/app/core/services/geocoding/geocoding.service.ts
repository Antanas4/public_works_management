import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface GeocodingResult {
  lat: number;
  lon: number;
}

@Injectable({
  providedIn: 'root'
})
export class GeocodingService {
  private readonly nominatimBaseUrl = 'https://nominatim.openstreetmap.org';

  constructor(private http: HttpClient) {
  }

  reverseGeocode(lat: number, lon: number): Observable<string | null> {
    const params = new HttpParams()
      .set('format', 'jsonv2')
      .set('lat', lat.toString())
      .set('lon', lon.toString());

    return this.http
      .get<any>(`${this.nominatimBaseUrl}/reverse`, { params })
      .pipe(map(result => result?.display_name ?? null));
  }

  forwardGeocode(address: string): Observable<GeocodingResult | null> {
    const params = new HttpParams()
      .set('format', 'jsonv2')
      .set('q', address)
      .set('limit', '1');

    return this.http.get<any[]>(`${this.nominatimBaseUrl}/search`, { params })
      .pipe(map(results => {
        const first = results && results.length > 0 ? results[0] : null;
        if (!first) {
          return null;
        }
        return {
          lat: Number(first.lat),
          lon: Number(first.lon)
        };
      }));
  }
}
