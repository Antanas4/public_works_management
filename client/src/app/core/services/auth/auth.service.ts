import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {BehaviorSubject, Observable, of} from "rxjs";
import {User} from "../../models/user.model";
import {catchError, tap} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api';

  private userSubject = new BehaviorSubject<User | null>(null);
  user$ = this.userSubject.asObservable();

  constructor(private _http: HttpClient) {}

  login(username: string, password: string): Observable<User> {
    const loginRequest = { username, password };

    return this._http.post<User>(`${this.apiUrl}/auth/login`, loginRequest, { withCredentials: true })
      .pipe(
        tap(user => this.userSubject.next(user))
      );
  }

  register(userData: any): Observable<User> {
    return this._http.post<User>(`${this.apiUrl}/auth/register`, userData, { withCredentials: true });
  }

  logout(): Observable<void> {
    return this._http.post<void>(`${this.apiUrl}/auth/logout`, {}, { withCredentials: true })
      .pipe(
        tap(() => this.userSubject.next(null))
      );
  }

  getCurrentUser(): User | null {
    return this.userSubject.value;
  }

  loadUser(): Observable<User | null> {
    return this._http.get<User>(`${this.apiUrl}/auth/me`, { withCredentials: true })
      .pipe(
        tap(user => this.userSubject.next(user)),
        catchError(() => {
          this.userSubject.next(null);
          return of(null);
        })
      );
  }

  isAuthenticated(): boolean {
    return this.userSubject.value != null;
  }
}