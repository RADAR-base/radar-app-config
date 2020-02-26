import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Config} from '@app/config/models/config';
import {ToastService} from '@app/shared/services/toast.service';
import {Observable} from 'rxjs';

/**
 * Config Service
 */
@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  constructor(private http: HttpClient, private toastService: ToastService) { }

  private getGlobalConfigByClientIdObservable(clientId): Observable<Config> {
    return this.http.get<Config>(`/api/global/config/${clientId}`);
  }

  async getGlobalConfigByClientId(clientId) {
    return await this.getGlobalConfigByClientIdObservable(clientId).toPromise()
      .then((data: any) => {
        const {config, defaults} = data;
        config.forEach(c => {
          if (defaults) {
            const defaultValues = defaults.filter(d => d.name === c.name && d.scope === 'global');
            if (defaultValues.length > 0) {
              c.default = defaultValues[0].value;
            }
          }
        });
        this.toastService.showSuccess(`Configurations of Application: ${clientId} loaded.`);
        return config;
      })
      .catch(e => {
        this.toastService.showError(e);
        console.log(e);
      })
      .finally(() => {});
  }


  private getConfigByProjectIdClientIdObservable(projectId, clientId): Observable<Config> {
    return this.http.get<Config>(`/api/projects/${projectId}/config/${clientId}`);
  }

  async getConfigByProjectIdClientId(projectId, clientId) {
    return await this.getConfigByProjectIdClientIdObservable(projectId, clientId).toPromise()
      .then((data: any) => {
        const {config, defaults} = data;
        config.forEach(c => {
          if (defaults) {
            const defaultValues = defaults.filter(d => d.name === c.name && d.scope === 'global');
            if (defaultValues.length > 0) {
              c.default = defaultValues[0].value;
            }
          }
        });
        this.toastService.showSuccess(`Configurations of Project: ${projectId} - Application: ${clientId} loaded.`);
        return config;
      })
      .catch(e => {
        this.toastService.showError(e);
        console.log(e);
      })
      .finally();
  }

  private getConfigByProjectIdClientIdUserIdObservable(projectId, clientId, userId) {}
  getConfigByProjectIdClientIdUserId(projectId, clientId, userId) {}



  postGlobalConfigByClientId(clientId, payload) {
    return this.http.post(`/api/global/config/${clientId}`, payload, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
  }

  postConfigByProjectIdAndClientId(projectId, clientId, payload) {
    return this.http.post(`/api/projects/${projectId}/config/${clientId}`, payload, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
  }

  postConfigByProjectIdAndClientIdAndUserId(projectId, clientId, userId, payload) {
    return this.http.post(`/api/projects/${projectId}/users/${userId}/config/${clientId}`, payload, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
  }
}
