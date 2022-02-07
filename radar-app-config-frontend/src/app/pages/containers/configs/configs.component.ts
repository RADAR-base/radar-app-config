import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {ProjectService} from '@app/pages/services/project.service';
import {ClientService} from '@app/pages/services/client.service';
import {UserService} from '@app/pages/services/user.service';
import {ConfigService} from '@app/pages/services/config.service';
import {Project} from '@app/pages/models/project';
import {Client} from '@app/pages/models/client';
import {User} from "@app/pages/models/user";
import {TranslateService} from "@app/shared/services/translate.service";
import {ConfigElement} from "@app/pages/models/config";

@Component({
  selector: 'app-configs',
  templateUrl: './configs.component.html',
})

export class ConfigsComponent implements OnInit {

  projectId;
  clientId;
  userId;
  configs: ConfigElement[];
  user: User | null;

  projects: Project[] | null;
  clients: Client[] | null;
  users: User[] | null;
  loading = true;

  constructor(
    public translate: TranslateService,
    private projectService: ProjectService,
    private clientService: ClientService,
    private userService: UserService,
    private configService: ConfigService,
    private activatedRoute: ActivatedRoute,
    private router: Router) {}

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

    this.updateUser();
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
    if (this.projectId === event.name) {
      return;
    } else {
      const queryParams: any = {
        ...this.activatedRoute.snapshot.queryParams,
        project: event.name,
      };
      delete queryParams.user;
      this.router.navigate(['/users'], {queryParams});
    }
  }

  onClientChange(event) {
    this.clientId = event.id;
    const tempObject = {...this.activatedRoute.snapshot.queryParams};
    tempObject.client = this.clientId;
    this.updateConfigs();
    this.router.navigate(['/configs'], {queryParams: tempObject});
  }

  onUserChange(event) {
    this.userId = event.id;
    const tempObject = {...this.activatedRoute.snapshot.queryParams};
    tempObject.user = this.userId;
    this.updateUser();
    this.updateConfigs();
    this.router.navigate(['/configs'], {queryParams: tempObject});
  }

  private updateUser() {
    if (this.userId) {
      this.user = this.users.find(u => u.id === this.userId) || {id: this.userId, name: this.userId};
    } else {
      this.user = null;
    }
  }

  async updateConfigs() {
    this.loading = true;
    if (this.projectId && this.clientId && this.userId){
      this.configs = await this.configService.getConfigByProjectIdUserIdClientId(this.projectId, this.userId, this.clientId);
    } else if (this.projectId && this.clientId && !this.userId) {
      this.configs = await this.configService.getConfigByProjectIdClientId(this.projectId, this.clientId);
    }
    this.loading = false;
  }

  onSave(event) {
    if (this.projectId && this.clientId && this.userId) {
      this.configService.postConfigByProjectIdAndClientIdAndUserId(this.projectId, this.clientId, this.userId, event.config)
        .subscribe((config) => this.configs = config);
    } else if (this.projectId && this.clientId && !this.userId) {
        this.configService.postConfigByProjectIdAndClientId(this.projectId, this.clientId, event.config)
        .subscribe((config) => this.configs = config);
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
