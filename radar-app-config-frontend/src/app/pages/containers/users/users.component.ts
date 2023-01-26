import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {ProjectService} from '@app/pages/services/project.service';
import {ClientService} from '@app/pages/services/client.service';
import {UserService} from '@app/pages/services/user.service';
import {Project, projectsToDropDown} from '@app/pages/models/project';
import {Client, clientsToDropDown} from '@app/pages/models/client';
import {User} from '@app/pages/models/user';
import {TranslateService} from "@app/shared/services/translate.service";
import {DropDownItem} from "@app/shared/components/drop-down/drop-down.component";
import {BackButtonOptions} from "@app/core/containers/left-sidebar/left-sidebar.component";

/**
 * Users Component
 */
@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
})
export class UsersComponent implements OnInit {

  projectId;
  clientId;

  projects: Project[];
  projectOptions: DropDownItem[] = []
  clients: Client[];
  clientOptions: DropDownItem[] = []
  users: User[];

  loading = true;

  constructor(
    public translate: TranslateService,
    private userService: UserService,
    private clientService: ClientService,
    private projectService: ProjectService,
    private activatedRoute: ActivatedRoute,
    private router: Router) {
    this.users = [];
  }

  async ngOnInit() {
    this.projectId = this.activatedRoute.snapshot.queryParams.project;
    this.clientId = this.activatedRoute.snapshot.queryParams.client;

    this.projects = await this.getProjects();
    this.projectOptions = projectsToDropDown(this.projects)
    this.clients = await this.getClients();
    this.clientOptions = clientsToDropDown(this.clients)
    await this.updateUsers()
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

  onProjectChange(event: string) {
    this.projectId = event;
    const queryParams = {
      ...this.activatedRoute.snapshot.queryParams,
      project: this.projectId,
    };
    this.updateUsers();
    this.router.navigate(['/users'], { queryParams });
  }

  onClientChange(event: string) {
    this.clientId = event;
    const queryParams = {
      ...this.activatedRoute.snapshot.queryParams,
      client: this.clientId,
    };
    this.updateUsers();
    this.router.navigate(['/users'], { queryParams });
  }

  async updateUsers() {
    this.loading = true;
    this.users = await this.userService.getUsersByProjectId(this.projectId);
    this.loading = false;
  }

  makeState() {
    return {projects: this.projects, clients: this.clients, users: this.users};
  }

  makeQueryParams(userId) {
    return {project: this.projectId, client: this.clientId, user: userId};
  }

  makeBackButton(): BackButtonOptions | null {
    if (!this.clientId || !this.projectId) { return null; }
    return {routerLink: ['/clients'], queryParams: {project: this.projectId}, name: 'Applications'};
  }
}
