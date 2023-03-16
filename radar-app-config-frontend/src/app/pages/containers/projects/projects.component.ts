import { Component, OnInit } from '@angular/core';
import {Project} from '@app/pages/models/project';
import {ProjectService} from '@app/pages/services/project.service';
import {TranslateService} from "@app/shared/services/translate.service";

/**
 * Projects Component
 */
@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
})

export class ProjectsComponent implements OnInit {

  loading = true;
  projects: Project[] = [];

  constructor(public translate: TranslateService, private projectService: ProjectService) {}

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
