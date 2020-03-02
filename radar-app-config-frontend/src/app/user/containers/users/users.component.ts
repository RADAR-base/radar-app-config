import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {ProjectService} from '@app/project/services/project.service';
import {ClientService} from '@app/client/services/client.service';
import {UserService} from '@app/user/services/user.service';
import {Project} from '@app/project/models/project';
import {Client} from '@app/client/models/client';
import {User} from '@app/user/models/user';
import strings from '@i18n/strings.json';

/**
 * Users Component
 */
@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  // styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {

  __ = strings;

  projectId;
  clientId;

  projects: [Project];
  clients: [Client];
  users: [User] | void;

  loading = true;

  constructor(
    private userService: UserService,
    private clientService: ClientService,
    private projectService: ProjectService,
    private activatedRoute: ActivatedRoute,
    private router: Router) {}

  async ngOnInit() {
    this.projectId = this.activatedRoute.snapshot.queryParams.project;
    this.clientId = this.activatedRoute.snapshot.queryParams.client;

    this.projects = await this.getProjects();
    this.clients = await this.getClients();

    this.updateUsers();
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
    this.updateUsers();
    this.router.navigate(['/configs'], {queryParams: tempObject});
  }

  onClientChange(event) {
    this.clientId = event.id;
    const tempObject = {...this.activatedRoute.snapshot.queryParams};
    tempObject.client = this.clientId;
    this.updateUsers();
    this.router.navigate(['/configs'], {queryParams: tempObject});
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

  makeBackButton() {
    if (!this.clientId || !this.projectId) { return; }
    return {routerLink: ['/clients'], queryParams: {project: this.projectId}, name: 'Applications'};
  }
}
