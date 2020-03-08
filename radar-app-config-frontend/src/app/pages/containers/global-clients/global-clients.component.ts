import { Component, OnInit } from '@angular/core';
import {Client} from '@app/pages/models/client';
import {ClientService} from '@app/pages/services/client.service';
import strings from '@i18n/strings.json';

@Component({
  selector: 'app-global-clients',
  templateUrl: './global-clients.component.html',
  // styleUrls: ['./global-clients.component.scss']
})

export class GlobalClientsComponent implements OnInit {
  __ = strings;
  loading = false;
  clients: [Client] | void;

  constructor(private clientService: ClientService) {}

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
