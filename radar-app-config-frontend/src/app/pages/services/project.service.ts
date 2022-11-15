import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Project} from '@app/pages/models/project';
import {ToastService} from '@app/shared/services/toast.service';
import {environment} from "@environments/environment";

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

  private getAllProjectsObservable(): Observable<[Project]> {
    return this.http.get<any>(`${environment.backendUrl}/projects`);
  }

  async getAllProjects(): Promise<[Project] | void> {
    return this.getAllProjectsObservable().toPromise()
      .then((data: any) => {
        const projects: [Project] = data.projects;
        projects.forEach(p => p.name = p.projectName);
        this.toastService.showSuccess('Projects loaded.');
        return projects;
      })
      .catch(e => {
        this.toastService.showError(e);
        console.log(e);
      });
  }
}
