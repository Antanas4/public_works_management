import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {User} from "../../models/user.model";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api'

  constructor(private _http: HttpClient) {
  }

  login(username: string, password: string): Observable<User> {
    const loginRequest = { username, password };
    return this._http.post<User>(`${this.apiUrl}/auth/login`, loginRequest, { withCredentials: true });
  }

  register(userData: any): Observable<User> {
    return this._http.post<User>(`${this.apiUrl}/auth/register`, userData, { withCredentials: true });
  }

  logout() {
    return this._http.post('/api/auth/logout', {}, { withCredentials: true });
  }
}