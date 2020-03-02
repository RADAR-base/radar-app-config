import { Component, OnInit } from '@angular/core';
import {Project} from '@app/project/models/project';
import {Client} from '@app/client/models/client';
import {ConfigService} from '@app/config/services/config.service';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastService} from '@app/shared/services/toast.service';
import {ProjectService} from '@app/project/services/project.service';
import {ClientService} from '@app/client/services/client.service';
import strings from '@i18n/strings.json';

@Component({
  selector: 'app-config-selection-page',
  templateUrl: './config-selection-page.component.html',
  // styleUrls: ['./config-selection-page.component.scss']
})
export class ConfigSelectionPageComponent implements OnInit {
  __ = strings;

  projectId;
  clientId;
  projects: [Project];
  clients: [Client];

  constructor(
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
    this.clients = await this.getClients();
  }

  getProjects() {
    if (!this.projectId) { return; }
    const {projects} = history.state;
    return (projects ? projects : this.projectService.getAllProjects());
  }

  getClients() {
    if (!this.clientId) { return; }
    const {clients} = history.state;
    return (clients ? clients : this.clientService.getAllClients());
  }

  onProjectChange(event) {
    this.projectId = event.id;
    const tempObject = {...this.activatedRoute.snapshot.queryParams};
    tempObject.project = this.projectId;
    this.router.navigate(['/configs'], {queryParams: tempObject});
  }

  onClientChange(event) {
    this.clientId = event.id;
    const tempObject = {...this.activatedRoute.snapshot.queryParams};
    tempObject.client = this.clientId;
    this.router.navigate(['/configs'], {queryParams: tempObject});
  }

  makeState() {
    return {projects: this.projects, clients: this.clients};
  }

  makeQueryParams() {
    return {project: this.projectId, client: this.clientId};
  }

  makeBackButton() {
    if (!this.clientId || !this.projectId) { return; }
    return {routerLink: ['/clients'], queryParams: {project: this.projectId}, name: 'Applications'};
  }
}
