import { Component, OnInit } from '@angular/core';
import {Project, projectsToDropDown} from '@app/pages/models/project';
import {Client, clientsToDropDown} from '@app/pages/models/client';
import {ConfigService} from '@app/pages/services/config.service';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastService} from '@app/shared/services/toast.service';
import {ProjectService} from '@app/pages/services/project.service';
import {ClientService} from '@app/pages/services/client.service';
import {TranslateService} from '@app/shared/services/translate.service';
import {DropDownItem} from "@app/shared/components/drop-down/drop-down.component";
import {BackButtonOptions} from "@app/core/containers/left-sidebar/left-sidebar.component";
// import strings from '@i18n/strings.json';

@Component({
  selector: 'app-config-selection-page',
  templateUrl: './config-selection-page.component.html',
  styleUrls: ['./config-selection-page.component.scss']
})
export class ConfigSelectionPageComponent implements OnInit {
  // __ = strings;

  projectId;
  clientId;
  projects: Project[];
  clients: Client[];

  projectOptions: DropDownItem[] = []
  clientOptions: DropDownItem[] = []

  constructor(
    public translate: TranslateService,
    private configService: ConfigService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private toastService: ToastService,
    private projectService: ProjectService,
    private clientService: ClientService) {}

  async ngOnInit() {
    this.projectId = this.activatedRoute.snapshot.queryParams.project;
    this.clientId = this.activatedRoute.snapshot.queryParams.client;

    this.projects = await this.getProjects();
    this.projectOptions = projectsToDropDown(this.projects)
    this.clients = await this.getClients();
    this.clientOptions = clientsToDropDown(this.clients)
  }

  async getProjects() {
    if (!this.projectId) return null;
    const { projects } = history.state;
    return (projects ? projects : await this.projectService.getAllProjects());
  }

  async getClients() {
    if (!this.clientId) return null;
    const { clients } = history.state;
    return (clients ? clients : await this.clientService.getAllClients());
  }

  onProjectChange(event: string) {
    this.projectId = event;
    const queryParams = {
      ...this.activatedRoute.snapshot.queryParams,
      project: this.projectId,
    };
    this.router.navigate(['/configs'], {queryParams});
  }

  onClientChange(event: string) {
    this.clientId = event;
    const queryParams = {
      ...this.activatedRoute.snapshot.queryParams,
      client: this.clientId,
    };
    this.router.navigate(['/configs'], {queryParams});
  }

  makeState() {
    return {
      projects: this.projects,
      clients: this.clients,
    };
  }

  makeQueryParams() {
    return {
      project: this.projectId,
      client: this.clientId,
    };
  }

  makeBackButton(): BackButtonOptions | null {
    if (!this.clientId || !this.projectId) {
      return null;
    }

    return {
      routerLink: ['/clients'],
      queryParams: {project: this.projectId},
      name: 'Applications',
    };
  }
}
