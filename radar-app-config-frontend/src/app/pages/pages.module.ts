import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {CoreModule} from '@app/core/core.module';
import {RouterModule} from '@angular/router';
import {SharedModule} from '@app/shared/shared.module';
import {GlobalClientsComponent} from "@app/pages/containers/global-clients/global-clients.component";
import {ClientsComponent} from "@app/pages/containers/clients/clients.component";
import {ConfigsComponent} from "@app/pages/containers/configs/configs.component";
import {GlobalConfigsComponent} from "@app/pages/containers/global-configs/global-configs.component";
import {ConfigsTableComponent} from "@app/pages/containers/configs-table/configs-table.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {ProjectsComponent} from "@app/pages/containers/projects/projects.component";
import {UsersComponent} from "@app/pages/containers/users/users.component";
import {PagesRoutingModule} from "@app/pages/pages-routing.module";
import {TranslateModule} from "@ngx-translate/core";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatDialogModule} from "@angular/material/dialog";
import {MatListModule} from "@angular/material/list";

@NgModule({
    declarations: [ClientsComponent, GlobalClientsComponent, ConfigsComponent, GlobalConfigsComponent, ConfigsTableComponent,
        ProjectsComponent,UsersComponent],
  imports: [
    CommonModule,
    CoreModule,
    RouterModule,
    SharedModule,
    FormsModule,
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    PagesRoutingModule,
    TranslateModule,
    MatTooltipModule,
    MatDialogModule,
    MatListModule

  ]
})
export class PagesModule { }
