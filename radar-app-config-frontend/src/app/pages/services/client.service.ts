import {firstValueFrom, Observable, of} from 'rxjs';
import { Injectable } from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Client} from '@app/pages/models/client';
import {ToastService} from '@app/shared/services/toast.service';
import {environment} from "@environments/environment";
import {catchError, map} from "rxjs/operators";

/**
 * Client Service
 */
@Injectable({
  providedIn: 'root'
})

export class ClientService {

  /**
   * constructor
   * @params http
   * toast service
   */
  constructor(private http: HttpClient, private toastService: ToastService) { }

  /**
   * getAllClientsObservable
   * @returns clients observable
   */
  private getAllClientsObservable(): Observable<{clients: Client[]}> {
    return this.http.get<{clients: Client[]}>(`${environment.backendUrl}/clients`);
  }

  /**
   * getAllClients
   * @returns clients
   */
  getAllClients(): Promise<Client[]> {
    return firstValueFrom(
        this.getAllClientsObservable().pipe(
          map(data => {
            if (!data.clients) {
              throw Error("Cannot load clients: " + data['error_description'])
            }
            this.toastService.showSuccess('Clients loaded.');
            return data.clients;
          }),
          catchError((e) => {
            this.toastService.showError(e);
            console.log(e);
            return of([]);
          })
        )
    );
  }
}
