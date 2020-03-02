import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {User} from '@app/user/models/user';
import {ToastService} from '@app/shared/services/toast.service';
import {Observable} from 'rxjs';
import {environment} from "@environments/environment";

/**
 * User Service
 */
@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient, private toastService: ToastService) {}

  private getUsersByProjectIdObservable(projectId): Observable<[User]> {
    return this.http.get<any>(`${environment.backendUrl}/projects/${projectId}/users/`);
  }

  async getUsersByProjectId(projectId): Promise<[User] | void> {
    return await this.getUsersByProjectIdObservable(projectId).toPromise()
      .then((data: any) => {
        const results: [User] = data.users;
        results.forEach(d => d.name = d.id);
        this.toastService.showSuccess(`Users of Project: ${projectId} loaded.`);
        return results;
      })
      .catch(e => {
        this.toastService.showError(e);
        console.log(e);
      })
      .finally(() => {});
  }
}
