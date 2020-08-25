import { Component, OnInit } from '@angular/core';
import {Client} from '@app/pages/models/client';
import {ClientService} from '@app/pages/services/client.service';
import {TranslateService} from "@app/shared/services/translate.service";

@Component({
  selector: 'app-global-clients',
  templateUrl: './global-clients.component.html',
})

export class GlobalClientsComponent implements OnInit {

  loading = false;
  clients: [Client] | void;

  constructor(public translate: TranslateService, private clientService: ClientService) {}

  ngOnInit() {
    this.updateClients();
  }

  async updateClients() {
    this.loading = true;
    this.clients = await this.clientService.getAllClients();
    this.loading = false;
  }

  makeState() {
    return {clients: this.clients};
  }

  makeQueryParams(clientId) {
    return {client: clientId};
  }
}
