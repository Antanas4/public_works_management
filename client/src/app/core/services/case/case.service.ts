import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {Case} from "../../models/case.model";
import {PaginationResponse} from "../../models/pagination-response.model";
import {Supplier} from "../../models/supplier.model";

@Injectable({
  providedIn: 'root'
})
export class CaseService {
  private apiUrl = 'http://localhost:8080/api/cases'

  constructor(private _http: HttpClient) {
  }

  getUserCases(paginationRequest: any): Observable<PaginationResponse<Case>> {
    let params = new HttpParams({fromObject: paginationRequest});

    return this._http.get<PaginationResponse<Case>>(`${this.apiUrl}/user`, {params, withCredentials: true});
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

    return this._http.post<Case>(this.apiUrl, formData, { withCredentials: true });
  }

  getCaseById(id: number): Observable<Case> {
    return this._http.get<Case>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  suggestSuppliersForCase(caseId: number): Observable<Supplier[]> {
    return this._http.get<any[]>(
      `${this.apiUrl}/${caseId}/suppliers`,
      { withCredentials: true }
    );
  }

  assignSupplierToCase(caseId: number, supplierId: number) {
    return this._http.put(
      `${this.apiUrl}/${caseId}/supplier/${supplierId}`,
      {},
      { withCredentials: true }
    );
  }
}
