import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {Case} from "../../models/case.model";
import {PaginationResponse} from "../../models/pagination-response.model";

@Injectable({
  providedIn: 'root'
})
export class CaseService {
  private apiUrl = 'http://localhost:8080/api/cases'

  constructor(private _http: HttpClient) {
  }

  getCasesByUserId(userId: number, paginationRequest: any): Observable<PaginationResponse<Case>> {
    let params = new HttpParams({fromObject: paginationRequest});

    return this._http.get<PaginationResponse<Case>>(`${this.apiUrl}/user/${userId}`, {params});
  }

  createCase(newCase: Case, photos: File[]): Observable<Case> {
    const formData = new FormData();

    formData.append(
      "case",
      new Blob([JSON.stringify(newCase)], {type: "application/json"})
    );

    photos.forEach(photo => {
      formData.append("photos", photo);
    });

    return this._http.post<Case>(this.apiUrl, formData);
  }

  getCaseById(id: number): Observable<Case> {
    return this._http.get<Case>(`${this.apiUrl}/${id}`);
  }
}
