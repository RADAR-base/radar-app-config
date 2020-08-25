import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
// import { ClientsComponent } from '../containers/clients/clients.component';
import {CoreModule} from '@app/core/core.module';
import {RouterModule} from '@angular/router';
import {SharedModule} from '@app/shared/shared.module';
// import { GlobalClientsComponent } from '../containers/global-clients/global-clients.component';
import {NgbDropdownModule, NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";
import {GlobalClientsComponent} from "@app/pages/containers/global-clients/global-clients.component";
import {ClientsComponent} from "@app/pages/containers/clients/clients.component";
import {ConfigsComponent} from "@app/pages/containers/configs/configs.component";
import {GlobalConfigsComponent} from "@app/pages/containers/global-configs/global-configs.component";
import {ConfigsTableComponent} from "@app/pages/containers/configs-table/configs-table.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {ProjectsComponent} from "@app/pages/containers/projects/projects.component";
// import {ProjectRoutingModule} from "@app/pages/project/project-routing.module";
import {UsersComponent} from "@app/pages/containers/users/users.component";
import {PagesRoutingModule} from "@app/pages/pages-routing.module";
import {TranslateModule} from "@ngx-translate/core";

@NgModule({
    declarations: [ClientsComponent, GlobalClientsComponent, ConfigsComponent, GlobalConfigsComponent, ConfigsTableComponent,
        ProjectsComponent,UsersComponent],
	imports: [
		CommonModule,
		CoreModule,
		RouterModule,
		SharedModule,
		NgbDropdownModule,
		// CommonModule,
		// RouterModule,
		// CoreModule,
		// SharedModule,
		FormsModule,
		ReactiveFormsModule,
		MatIconModule,
		MatButtonModule,
		// NgbDropdownModule,
		// CommonModule,
		// ProjectRoutingModule,
		// CoreModule,
		// CommonModule,
		// CoreModule,
		// RouterModule,
		// SharedModule,
		// NgbDropdownModule
		PagesRoutingModule,
		TranslateModule,
		NgbTooltipModule

	]
})
export class PagesModule { }
