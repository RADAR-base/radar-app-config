import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Project} from '@app/project/models/project';
import {ProjectService} from '@app/project/services/project.service';
import {Client} from '@app/client/models/client';
import {ClientService} from '@app/client/services/client.service';
import strings from '@i18n/strings.json';

@Component({
  selector: 'app-clients',
  templateUrl: './clients.component.html',
  styleUrls: ['./clients.component.scss']
})
export class ClientsComponent implements OnInit {
  private __ = strings;
  private loading = false;
  private projectId;
  private clients: [Client] | void;
  private projects: [Project];

  constructor(
    private clientService: ClientService,
    private projectService: ProjectService,
    private activatedRoute: ActivatedRoute,
    private router: Router) {}

  async ngOnInit() {
    this.projectId = this.activatedRoute.snapshot.queryParams.project;
    this.projects = await this.getProjects();
    this.updateClients();
  }

  getProjects() {
    if (!this.projectId) { return; }
    const {projects} = history.state;
    return (projects ? projects : this.projectService.getAllProjects());
  }

  onProjectChange(d) {
    this.updateClients();
    this.projectId = d.id;
    this.router.navigate(['/clients'], {queryParams: {project: this.projectId}});
  }

  async updateClients() {
    this.loading = true;
    this.clients = await this.clientService.getAllClients();
    this.loading = false;
  }

  makeState() {
    return {projects: this.projects, clients: this.clients};
  }

  makeQueryParams(clientId) {
    return {project: this.projectId, client: clientId};
  }

  makeBackButton() {
    if (!this.projectId) { return; }
    return {routerLink: ['/projects'], queryParams: '', name: 'Projects'};
  }
}
