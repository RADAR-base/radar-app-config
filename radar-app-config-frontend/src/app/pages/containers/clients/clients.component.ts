import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Project} from '@app/pages/models/project';
import {ProjectService} from '@app/pages/services/project.service';
import {Client} from '@app/pages/models/client';
import {ClientService} from '@app/pages/services/client.service';
import {TranslateService} from '@app/shared/services/translate.service';

@Component({
  selector: 'app-clients',
  templateUrl: './clients.component.html',
})

export class ClientsComponent implements OnInit {

  loading = false;
  projectId;
  clients: Client[];
  projects: Project[];

  constructor(
    public translate: TranslateService,
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
    console.log(d);
    this.updateClients();
    this.projectId = d.name;
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
