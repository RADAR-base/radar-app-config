import { Component, OnInit } from '@angular/core';
import {Client} from '@app/client/models/client';
import {ClientService} from '@app/client/services/client.service';
import strings from '@i18n/strings.json';

@Component({
  selector: 'app-global-clients',
  templateUrl: './global-clients.component.html',
  styleUrls: ['./global-clients.component.scss']
})
export class GlobalClientsComponent implements OnInit {
  private __ = strings;
  private loading = false;
  private clients: [Client] | void;

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
