import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient, HttpParams} from '@angular/common/http';
import {map} from 'rxjs/operators';
import * as jwt_decode from 'jwt-decode';
import {AuthResponse} from '@app/auth/models/auth';
import {Roles} from '@app/auth/enums/roles.enum';
import {User} from '@app/auth/models/user';
import {environment} from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private currentUserSubject: BehaviorSubject<AuthResponse>;
  public currentUser: Observable<AuthResponse>;

  constructor(private http: HttpClient) {
    this.currentUserSubject = new BehaviorSubject<AuthResponse>(JSON.parse(localStorage.getItem('currentUser')));
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): AuthResponse {
    return this.currentUserSubject.value;
  }

  public get isAdmin(): boolean {
    if (this.currentDecodedUserValue) { return this.currentDecodedUserValue.role === Roles.SYSTEM_ADMIN; }
    return false;
  }

  public get currentDecodedUserValue(): User {
    const currentUser = this.currentUserValue;
    const isLoggedIn = Boolean(currentUser && currentUser.access_token);

    if (isLoggedIn) {
      const jwtDecoded = jwt_decode(currentUser.access_token);
      return {username: jwtDecoded.user_name, role: jwtDecoded.authorities[0] };
    }
    return null;
  }

  logout() {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  processLogin(authCode: any) {
    const payload = new HttpParams()
      .append('grant_type', 'authorization_code')
      .append('code', authCode)
      .append('redirect_uri', environment.authCallback)
      .append('client_id', environment.clientId);
    return this.http.post(`${environment.authAPI}/token`, payload, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    }).pipe(map((user: AuthResponse) => {
      if (user && user.access_token) {
        localStorage.setItem('currentUser', JSON.stringify(user));
        this.currentUserSubject.next(user);
      }
      return user;
    }));
  }

  authorize() {
    console.log('authorize');
    if (environment.envName === 'mock' || environment.envName === 'mock-researcher') {
      // tslint:disable-next-line:max-line-length
      return this.http.get(`${environment.authAPI}/authorize?client_id=${environment.clientId}&response_type=code&redirect_uri=${environment.authCallback}`)
        .subscribe();
    } else {
      window.location.href =
        `${environment.authAPI}/authorize?client_id=${environment.clientId}&response_type=code&redirect_uri=${environment.authCallback}`;
    }
  }
}
