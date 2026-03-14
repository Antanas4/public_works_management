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
    return this._http.post<User>(`${this.apiUrl}/auth/login`, loginRequest);
  }

  register(userData: any): Observable<User> {
    return this._http.post<User>(`${this.apiUrl}/auth/register`, userData);
  }

  isUsernameAvailable(username: string): Observable<boolean> {
    return this._http.get<boolean>(`${this.apiUrl}/auth/username-available/${username}`);
  }

  isEmailAvailable(email: string): Observable<boolean> {
    return this._http.get<boolean>(`${this.apiUrl}/auth/email-available/${email}`);
  }
}