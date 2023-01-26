import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {Client, clientsToDropDown} from '@app/pages/models/client';
import {ConfigService} from '@app/pages/services/config.service';
import {ToastService} from '@app/shared/services/toast.service';
import {ClientService} from '@app/pages/services/client.service';
import {TranslateService} from "@app/shared/services/translate.service";
import {ConfigElement} from "@app/pages/models/config";
import {DropDownItem} from "@app/shared/components/drop-down/drop-down.component";
import {BackButtonOptions} from "@app/core/containers/left-sidebar/left-sidebar.component";

@Component({
  selector: 'app-global-configs',
  templateUrl: './global-configs.component.html',
})

export class GlobalConfigsComponent implements OnInit {

  clientId: string;
  configs: ConfigElement[];
  clients: Client[] | null;
  clientOptions: DropDownItem[]
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
    this.clientOptions = clientsToDropDown(this.clients);
    await this.updateConfigs();
  }

  async getClients() {
    if (!this.clientId) return null;
    const {clients} = history.state;
    return (clients ? clients : await this.clientService.getAllClients());
  }

  onClientChange(event: string) {
    this.clientId = event;
    this.updateConfigs();
    const queryParams =  {client: this.clientId}
    this.router.navigate(['/global-configs'], { queryParams });
  }

  async updateConfigs() {
    this.loading = true;
    this.configs = await this.configService.getGlobalConfigByClientId(this.clientId);
    this.loading = false;
  }

  onSave(event) {
    this.configService.postGlobalConfigByClientId(this.clientId, event.config).subscribe((config) => {
      this.configs = config;
    });
  }

  makeBackButton(): BackButtonOptions | null {
    if (!this.clientId) { return null; }
    return {routerLink: ['/global-clients'], queryParams: {}, name: 'Applications'};
  }
}
