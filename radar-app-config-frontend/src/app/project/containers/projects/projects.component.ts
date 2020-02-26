import { Component, OnInit } from '@angular/core';
import {Project} from '@app/project/models/project';
import {ProjectService} from '@app/project/services/project.service';
import strings from '@i18n/strings.json';

/**
 * Projects Component
 */
@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.scss']
})
export class ProjectsComponent implements OnInit {
  private __ = strings;
  private loading = true;
  private projects: [Project] | void;

  constructor(private projectService: ProjectService) {}

  async ngOnInit() {
    this.projects = await this.projectService.getAllProjects();
    this.loading = false;
  }

  makeState() {
    return {projects: this.projects};
  }

  makeQueryParams(projectId) {
    return {project: projectId};
  }
}
