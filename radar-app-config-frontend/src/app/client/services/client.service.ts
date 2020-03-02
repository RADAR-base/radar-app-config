import { Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Client} from '@app/client/models/client';
import {ToastService} from '@app/shared/services/toast.service';
import {environment} from "@environments/environment";

/**
 * Client Service
 */
@Injectable({
  providedIn: 'root'
})
export class ClientService {

  constructor(private http: HttpClient, private toastService: ToastService) { }


  private getAllClientsObservable(): Observable<[Client]> {
    return this.http.get<any>(`${environment.backendUrl}/clients`);
  }

  async getAllClients(): Promise<[Client] | void> {
    return await this.getAllClientsObservable().toPromise()
      .then((data: any) => {
        const clients: [Client] = data.clients;
        clients.forEach(c => c.id = c.name = c.clientId);
        this.toastService.showSuccess('Clients loaded.');
        return clients;
      })
      .catch(e => {
        this.toastService.showError(e);
        console.log(e);
      })
      .finally(() => {});
  }
}
