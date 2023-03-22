import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom, Observable, of} from 'rxjs';
import {Project} from '@app/pages/models/project';
import {ToastService} from '@app/shared/services/toast.service';
import {environment} from "@environments/environment";
import {catchError, first, map} from "rxjs/operators";

/**
 * Project Service
 */
@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  constructor(
    private http: HttpClient,
    private toastService: ToastService,
  ) {}

  private getAllProjectsObservable(): Observable<{projects: Project[]}> {
    return this.http.get<{projects: Project[]}>(`${environment.backendUrl}/projects`);
  }

  getAllProjects(): Promise<Project[]> {
    return firstValueFrom(this.getAllProjectsObservable().pipe(
      map((data: any) => {
        if (!data.projects) {
          throw Error("Cannot load projects: " + data['error_description'])
        }
        this.toastService.showSuccess('Projects loaded.');
        return data.projects;
      }),
      catchError(e => {
        this.toastService.showError(e);
        console.log(e);
        return of([]);
      }),
    ));
  }
}
