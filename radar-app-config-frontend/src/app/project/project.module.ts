import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {CoreModule} from '@app/core/core.module';
import {ProjectsComponent} from '@app/project/containers/projects/projects.component';
import {ProjectRoutingModule} from '@app/project/project-routing.module';

@NgModule({
  declarations: [ProjectsComponent],
  imports: [
    CommonModule,
    ProjectRoutingModule,
    CoreModule
  ]
})
export class ProjectModule { }
