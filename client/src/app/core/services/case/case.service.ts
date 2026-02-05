import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {Case} from "../../models/case.model";
import {PaginationResponse} from "../../models/pagination-response.model";

@Injectable({
    providedIn: 'root'
})
export class CaseService {
    private apiUrl = 'http://localhost:8085/customer-1.0/api/cases'

    constructor(private _http: HttpClient) {
    }

    getCasesByUserId(userId: number, paginationRequest: any): Observable<PaginationResponse<Case>> {
        let params = new HttpParams({ fromObject: paginationRequest });

        return this._http.get<PaginationResponse<Case>>(`${this.apiUrl}/user/${userId}`, {params});
    }

    createCase(newCase: Case): Observable<Case> {
        return this._http.post<Case>(this.apiUrl, newCase);
    }

    getCaseById(id: number): Observable<Case> {
        return this._http.get<Case>(`${this.apiUrl}/${id}`);
    }
}
