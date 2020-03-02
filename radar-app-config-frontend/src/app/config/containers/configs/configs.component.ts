import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {ProjectService} from '@app/project/services/project.service';
import {ClientService} from '@app/client/services/client.service';
import {UserService} from '@app/user/services/user.service';
import {ConfigService} from '@app/config/services/config.service';
import {Project} from '@app/project/models/project';
import {Client} from '@app/client/models/client';
import {ToastService} from '@app/shared/services/toast.service';
import strings from '@i18n/strings.json';
import {User} from "@app/user/models/user";

@Component({
  selector: 'app-configs',
  templateUrl: './configs.component.html',
  // styleUrls: ['./configs.component.scss']
})

export class ConfigsComponent implements OnInit {
  __ = strings;

  projectId;
  clientId;
  userId;
  configs;

  projects: [Project] | null;
  clients: [Client] | null;
  users: [User] | null;
  loading = true;

  constructor(
    private projectService: ProjectService,
    private clientService: ClientService,
    private userService: UserService,
    private configService: ConfigService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private toastService: ToastService) {}

  async ngOnInit() {
    this.projectId = this.activatedRoute.snapshot.queryParams.project;
    this.clientId = this.activatedRoute.snapshot.queryParams.client;
    this.userId = this.activatedRoute.snapshot.queryParams.user;

    this.projects = await this.getProjects();
    this.clients = await this.getClients();
    console.log(this.clients);
    this.users = await this.getUsers();
    console.log(this.users);

    this.updateConfigs();
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

  getUsers() {
    console.log(this.userId);
    if (!this.userId) { return; }
    const {users} = history.state;
    return (users ? users : this.userService.getUsersByProjectId(this.projectId));
  }

  onProjectChange(event) {
    this.projectId = event.id;
    const tempObject = {...this.activatedRoute.snapshot.queryParams};
    tempObject.project = this.projectId;
    this.updateConfigs();
    this.router.navigate(['/configs'], {queryParams: tempObject});
  }

  onClientChange(event) {
    this.clientId = event.id;
    const tempObject = {...this.activatedRoute.snapshot.queryParams};
    tempObject.client = this.clientId;
    this.updateConfigs();
    this.router.navigate(['/configs'], {queryParams: tempObject});
  }

  onUserChange(event) {
    this.clientId = event.id;
    const tempObject = {...this.activatedRoute.snapshot.queryParams};
    tempObject.client = this.clientId;
    this.updateConfigs();
    this.router.navigate(['/configs'], {queryParams: tempObject});
  }

  async updateConfigs() {
    this.loading = true;
    this.configs = await this.configService.getConfigByProjectIdClientId(this.projectId, this.clientId);
    this.loading = false;
  }

  onSave(event) {
    const newConfigArray = event.config.map(c => ({name: c.name, value: c.value}));
    if (this.projectId && this.clientId && this.userId) {
      this.configService.postConfigByProjectIdAndClientIdAndUserId(this.projectId, this.clientId, this.userId, {config: newConfigArray})
        .subscribe(() => {
          // tslint:disable-next-line:max-line-length
          this.toastService.showSuccess(`Configurations of Project: ${this.projectId} - Application: ${this.clientId} - User: ${this.userId} changed.`);
        });
    } else if (this.projectId && this.clientId && !this.userId) {
      this.configService.postConfigByProjectIdAndClientId(this.projectId, this.clientId, {config: newConfigArray})
        .subscribe(() => {
          this.toastService.showSuccess(`Configurations of Project: ${this.projectId} - Application: ${this.clientId} changed.`);
        });
    }
  }

  makeBackButton() {
    if (!this.clientId || !this.projectId) { return; }
    return {routerLink: ['/clients'], queryParams: {project: this.projectId}, name: 'Applications'};
  }
}
