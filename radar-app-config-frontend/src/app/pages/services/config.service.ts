import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Config} from '@app/pages/models/config';
import {ToastService} from '@app/shared/services/toast.service';
import {Observable} from 'rxjs';
import {environment} from "@environments/environment";

/**
 * Config Service
 */
@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  constructor(private http: HttpClient, private toastService: ToastService) { }

  private getGlobalConfigByClientIdObservable(clientId): Observable<Config> {
    return this.http.get<Config>(`${environment.backendUrl}/global/config/${clientId}`);
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
    console.log(1);
    return this.http.get<Config>(`/api/projects/${projectId}/config/${clientId}`);
  }

  /*
  {
    "clientId": "{clientId}",
    "scope": "project.projectA",
    "config": [
      {"name": "plugins", "value": "A B"}
    ],
    "defaults": [
      {"name": "plugins", "value", "A", "scope": "global"
    ]
  }
  */
  async getConfigByProjectIdClientId(projectId, clientId) {
    console.log(2);
    return await this.getConfigByProjectIdClientIdObservable(projectId, clientId).toPromise()
      .then((data: any) => {
        console.log(data);
        const result = [];
        const {config, defaults} = data;
        if(defaults) {
          defaults.forEach(d => {
            d.default = d.value;
            result.push(d);
          });
        }
        if(config) {
          config.forEach(c => {
            console.log(c);
            const matchedItem = result.filter(d => c.name === d.name);
            console.log(matchedItem);
            if (matchedItem.length > 0) {

              const index = result.indexOf(matchedItem[0]);
              console.log(index);
              result[index].default = result[index].value;
              result[index].value = c.value;
            } else {
              result.push(c);
            }
          });
        }
        console.log(result);
        this.toastService.showSuccess(`Configurations of Project: ${projectId} - Application: ${clientId} loaded.`);
        return result;
      })
      .catch(e => {
        this.toastService.showError(e);
        console.log(e);
      })
      .finally();
  }

  // private getConfigByProjectIdClientIdUserIdObservable(projectId, clientId, userId) {}
  // getConfigByProjectIdClientIdUserId(projectId, clientId, userId) {}



  postGlobalConfigByClientId(clientId, payload) {
    console.log('payload', payload);
    return this.http.post(`/api/global/config/${clientId}`, payload, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
  }

  /*
  POST /projects/{projectId}/config/{clientId}
  {
    "config": [
      {"name": "rate", "value": "1"}
      {"name": "plugins", "value": "A B"}
    ]
  }
  */
  postConfigByProjectIdAndClientId(projectId, clientId, payload) {
    console.log('payload', payload);
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
