import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Supplier} from "../../models/supplier.model";

@Injectable({
  providedIn: 'root',
})
export class SupplierService {

  private apiUrl = 'http://localhost:8080/api/admin/suppliers';

  constructor(private http: HttpClient) {}

  getAllSuppliers(): Observable<Supplier[]> {
    return this.http.get<Supplier[]>(
      this.apiUrl,
      { withCredentials: true }
    );
  }

  assignSupplierToCase(caseId: number, supplier: Supplier) {
    return this.http.put(
      `${this.apiUrl}/assign-supplier-to-case/${caseId}`,
      supplier,
      { withCredentials: true }
    );
  }
}
