import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {User} from '@app/pages/models/user';
import {ToastService} from '@app/shared/services/toast.service';
import {firstValueFrom, Observable} from 'rxjs';
import {environment} from "@environments/environment";

/**
 * User Service
 */
@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient, private toastService: ToastService) {}

  private getUsersByProjectIdObservable(projectId): Observable<{users: User[]}> {
    return this.http.get<{users: User[]}>(`${environment.backendUrl}/projects/${encodeURIComponent(projectId)}/users/`);
  }

  async getUsersByProjectId(projectId): Promise<User[]> {
    return firstValueFrom(this.getUsersByProjectIdObservable(projectId))
      .then((data: any) => {
        if (!data.users) {
          throw Error("Cannot load users: " + data['error_description'])
        }
        const results: User[] = data.users;
        results.forEach(d => {
            if (d.externalUserId) {
                d.name = d.externalUserId + ' (' + d.id + ')';
            } else {
                d.name = d.id;
            }
        });
        this.toastService.showSuccess(`Users of Project: ${projectId} loaded.`);
        return results;
      })
      .catch(e => {
        this.toastService.showError(e);
        console.log(e);
        return [] as User[];
      })
      .finally(() => {});
  }
}
