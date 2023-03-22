import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Config, ConfigElement} from '@app/pages/models/config';
import {ToastService} from '@app/shared/services/toast.service';
import {Observable, pipe, OperatorFunction, firstValueFrom} from 'rxjs';
import {environment} from '@environments/environment';
import {map, tap} from 'rxjs/operators';

/**
 * Config Service
 */
@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  constructor(private http: HttpClient, private toastService: ToastService) { }

  private getGlobalConfigByClientIdObservable(clientId): Observable<Config> {
    return this.http.get<Config>(this.url(clientId));
  }

  async getGlobalConfigByClientId(clientId) {
      return firstValueFrom(this.getGlobalConfigByClientIdObservable(clientId).pipe(
          this.mapConfigResponse(`Configurations of Application: ${clientId} loaded.`),
      ));
  }

  private getConfigByProjectIdClientIdObservable(projectId, clientId): Observable<Config> {
      return this.http.get<Config>(this.url(clientId, projectId));
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
    return firstValueFrom(this.getConfigByProjectIdClientIdObservable(projectId, clientId).pipe(
        this.mapConfigResponse(`Configurations of Project: ${projectId} - Application: ${clientId} loaded.`),
    ));
  }

  private getConfigByProjectIdUserIDClientIdObservable(projectId, userId, clientId): Observable<Config> {
    return this.http.get<Config>(this.url(clientId, projectId, userId));
  }

  /*
  {
    "clientId": "appconfig",
    "scope": "user.34826646-c49d-4a3e-b873-42d19ac1d5df",
    "config": [
        {"name": "plugins", "value": "A B"}
    ],
    "defaults": [
        {"name": "plugins", "value", "A", "scope": "project.radar-base-1"
    ]
  }
  */
  async getConfigByProjectIdUserIdClientId(projectId, userId, clientId) {
    return firstValueFrom(this.getConfigByProjectIdUserIDClientIdObservable(projectId, userId, clientId).pipe(
        this.mapConfigResponse(`Configurations of Project: ${projectId} - Participant: ${userId} - Application: ${clientId} loaded.`),
    ));
  }


  postGlobalConfigByClientId(clientId, payload: ConfigElement[]): Observable<ConfigElement[]> {
    console.log('payload', payload);
    return this.http.post<Config>(this.url(clientId), this.configToServerConfig(payload), {
      headers: {
        'Content-Type': 'application/json'
      }
    }).pipe(
        this.mapConfigResponse(`Global configurations of Application: ${clientId} updated.`),
    );
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
  postConfigByProjectIdAndClientId(projectId, clientId, payload: ConfigElement[]): Observable<ConfigElement[]> {
    return this.http.post<Config>(this.url(clientId, projectId), this.configToServerConfig(payload), {
      headers: {
        'Content-Type': 'application/json'
      }
    }).pipe(
        this.mapConfigResponse(`Configurations of Project ${projectId} - Application: ${clientId} updated.`),
    );
  }

  postConfigByProjectIdAndClientIdAndUserId(projectId, clientId, userId, payload: ConfigElement[]): Observable<ConfigElement[]> {
    return this.http.post<Config>(this.url(clientId, projectId, userId), this.configToServerConfig(payload), {
      headers: {
        'Content-Type': 'application/json'
      }
    }).pipe(
        this.mapConfigResponse(`Configurations of Project ${projectId} - Participant ${userId} - Application: ${clientId} updated.`),
    );
  }

  private mapConfigResponse(successMessage: string): OperatorFunction<Config, ConfigElement[]> {
      return pipe(
          map(config => this.serverConfigToConfig(config)),
          tap({
              next: () => this.toastService.showSuccess(successMessage),
              error: e => {
                  this.toastService.showError(e);
                  console.log(e);
              },
          }),
      );
  }

  private configToServerConfig(elements: ConfigElement[]): Config {
    return {
      config: elements
          .filter(c => c.value !== c.default)
          .map(({name, value}) => ({name, value})),
    };
  }

  private url(clientId: string, projectId?: string, userId?: string): string {
      const configPath = `config/${encodeURIComponent(clientId)}`;
      const projectPath = projectId ? `projects/${encodeURIComponent(projectId)}` : '';
      const userPath = userId ? `users/${encodeURIComponent(userId)}` : '';
      if (userPath) {
          return `${environment.backendUrl}/${projectPath}/${userPath}/${configPath}`;
      } else if (projectPath) {
          return `${environment.backendUrl}/${projectPath}/${configPath}`;
      } else {
          return `${environment.backendUrl}/global/${configPath}`;
      }
  }

  private serverConfigToConfig(serverConfig: Config): ConfigElement[] {
    const {config, defaults} = serverConfig;
    let result: ConfigElement[] = [];
    if (defaults) {
      result = defaults.map(c => ({...c, default: c.value}));
    }
    if (config) {
      config.forEach(c => {
        const matchedItem = result.find(d => c.name === d.name);
        if (matchedItem) {
          matchedItem.value = c.value;
        } else {
          result.push(c);
        }
      });
    }
    return result;
  }
}
