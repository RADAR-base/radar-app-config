import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Params, Router} from '@angular/router';
import {ProjectService} from '@app/pages/services/project.service';
import {ClientService} from '@app/pages/services/client.service';
import {UserService} from '@app/pages/services/user.service';
import {ConfigService} from '@app/pages/services/config.service';
import {Project, projectsToDropDown} from '@app/pages/models/project';
import {Client, clientsToDropDown} from '@app/pages/models/client';
import {User, usersToDropDown} from "@app/pages/models/user";
import {TranslateService} from "@app/shared/services/translate.service";
import {ConfigElement} from "@app/pages/models/config";
import {DropDownItem} from "@app/shared/components/drop-down/drop-down.component";
import {BackButtonOptions} from "@app/core/containers/left-sidebar/left-sidebar.component";

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
  projectOptions: DropDownItem[] | null = null;
  clients: Client[] | null;
  clientOptions: DropDownItem[] | null = null;
  users: User[] | null;
  userOptions: DropDownItem[] | null = null;
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
    this.activatedRoute.queryParams.subscribe(async params => {
      await this.init(params);
    });
  }

  async init(params: Params){
    this.projectId = params.project;
    this.clientId = params.client;
    this.userId = params.user;

    this.projects = await this.getProjects();
    this.projectOptions = projectsToDropDown(this.projects);
    this.clients = await this.getClients();
    this.clientOptions = clientsToDropDown(this.clients);
    this.users = await this.getUsers();
    this.userOptions = usersToDropDown(this.users);

    this.updateUser();
    await this.updateConfigs()
  }

  async getProjects() {
    if (!this.projectId) return null;
    const {projects} = history.state;
    return (projects ? projects : await this.projectService.getAllProjects());
  }

  async getClients() {
    if (!this.clientId) return null;
    const {clients} = history.state;
    return (clients ? clients : await this.clientService.getAllClients());
  }

  async getUsers() {
    if (!this.projectId || !this.userId) return null;
    const {users} = history.state;
    return (users ? users : await this.userService.getUsersByProjectId(this.projectId));
  }

  onProjectChange(event: string) {
    if (this.projectId === event) {
      return;
    } else {
      const queryParams: any = {
        ...this.activatedRoute.snapshot.queryParams,
        project: event,
      };
      if (this.userId) {
        delete queryParams.user;
        this.router.navigate(['/users'], {queryParams});
      } else {
        this.router.navigate(['/configs'], {queryParams});
      }
    }
  }

  onClientChange(event: string) {
    this.clientId = event;
    const queryParams = {
      ...this.activatedRoute.snapshot.queryParams,
      client: this.clientId,
    };
    this.updateConfigs();
    this.router.navigate(['/configs'], {queryParams});
  }

  onUserChange(event: string) {
    this.userId = event;
    const queryParams = {
      ...this.activatedRoute.snapshot.queryParams,
      user: this.userId,
    };
    this.updateUser();
    this.updateConfigs();
    this.router.navigate(['/configs'], {queryParams});
  }

  private updateUser() {
    let foundUser = null;
    if (this.users) {
      foundUser = this.users.find(u => u.id === this.userId)
    }
    if (!foundUser && this.userId) {
      foundUser = {
        id: this.userId,
        name: this.userId,
      }
    }
    this.user = foundUser;
  }

  async updateConfigs() {
    this.loading = true;
    if (this.projectId && this.clientId && this.userId) {
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

  makeBackButton(): BackButtonOptions | null {
    if (!this.clientId || !this.projectId) { return null; }
    return {routerLink: ['/clients'], queryParams: {project: this.projectId}, name: 'Applications'};
  }
}
