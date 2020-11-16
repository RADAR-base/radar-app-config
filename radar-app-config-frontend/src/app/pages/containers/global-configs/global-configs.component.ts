import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {Client} from '@app/pages/models/client';
import {ConfigService} from '@app/pages/services/config.service';
import {ToastService} from '@app/shared/services/toast.service';
import {ClientService} from '@app/pages/services/client.service';
import {TranslateService} from "@app/shared/services/translate.service";

@Component({
  selector: 'app-global-configs',
  templateUrl: './global-configs.component.html',
})

export class GlobalConfigsComponent implements OnInit {

  clientId: string;
  configs;
  clients: [Client] | null;
  loading = true;

  constructor(
    public translate: TranslateService,
    private clientService: ClientService,
    private configService: ConfigService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private toastService: ToastService) {}

  async ngOnInit() {
    this.clientId = this.activatedRoute.snapshot.queryParams.client;
    this.clients = await this.getClients();
    this.updateConfigs();
  }

  getClients() {
    if (!this.clientId) { return; }
    const {clients} = history.state;
    return (clients ? clients : this.clientService.getAllClients());
  }

  onClientChange(event) {
    console.log("change", event);
    this.clientId = event.id;
    this.updateConfigs();
    this.router.navigate(['/global-configs'], {queryParams: {client: this.clientId}}).then(() => {});
  }

  async updateConfigs() {
    this.loading = true;
    this.configs = await this.configService.getGlobalConfigByClientId(this.clientId);
    this.loading = false;
  }

  onSave(event) {
    const newConfigArray = event.config.map(c => ({name: c.name, value: c.value}));
    this.configService.postGlobalConfigByClientId(this.clientId, {config: newConfigArray}).subscribe(() => {
      this.toastService.showSuccess(`Global Configurations of App: ${this.clientId} changed.`);
    });
  }

  makeBackButton() {
    if (!this.clientId) { return; }
    return {routerLink: ['/global-clients'], queryParams: {}, name: 'Applications'};
  }
}
