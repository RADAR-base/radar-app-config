import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {ProjectService} from '@app/pages/services/project.service';
import {ClientService} from '@app/pages/services/client.service';
import {UserService} from '@app/pages/services/user.service';
import {ConfigService} from '@app/pages/services/config.service';
import {Project} from '@app/pages/models/project';
import {Client} from '@app/pages/models/client';
import {ToastService} from '@app/shared/services/toast.service';
import {User} from "@app/pages/models/user";
import {TranslateService} from "@app/shared/services/translate.service";

@Component({
  selector: 'app-configs',
  templateUrl: './configs.component.html',
})

export class ConfigsComponent implements OnInit {

  projectId;
  clientId;
  userId;
  configs;

  projects: [Project] | null;
  clients: [Client] | null;
  users: [User] | null;
  loading = true;

  constructor(
    public translate: TranslateService,
    private projectService: ProjectService,
    private clientService: ClientService,
    private userService: UserService,
    private configService: ConfigService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private toastService: ToastService) {console.log('cons');}

  async ngOnInit() {
    this.activatedRoute.queryParams.subscribe(() => {
      this.init();
    });
  }

  async init(){
    this.projectId = this.activatedRoute.snapshot.queryParams.project;
    this.clientId = this.activatedRoute.snapshot.queryParams.client;
    this.userId = this.activatedRoute.snapshot.queryParams.user;

    this.projects = await this.getProjects();
    this.clients = await this.getClients();
    this.users = await this.getUsers();

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
    if (!this.userId) { return; }
    const {users} = history.state;
    return (users ? users : this.userService.getUsersByProjectId(this.projectId));
  }

  onProjectChange(event) {
    this.projectId = event.name;
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

  isActive(name){
    switch (name){
      case 'group':
        return false;
      case 'user':
        return !!this.userId;
      case 'config':
        return !!(!this.userId && this.clientId && this.projectId);
    }
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
