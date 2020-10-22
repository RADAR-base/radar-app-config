import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './containers/navbar/navbar.component';
import {MatIconModule} from '@angular/material/icon';
import { FooterComponent } from './containers/footer/footer.component';
import { PageNotFoundComponent } from './containers/page-not-found/page-not-found.component';
import { AdminSelectionPageComponent } from './containers/admin-selection-page/admin-selection-page.component';
import {CoreRoutingModule} from '@app/core/core-routing.module';
import { LeftSidebarComponent } from './containers/left-sidebar/left-sidebar.component';
import { ConfigSelectionPageComponent } from './containers/config-selection-page/config-selection-page.component';
import {SharedModule} from '@app/shared/shared.module';
import {MatButtonModule} from "@angular/material/button";
import {TranslateModule} from "@ngx-translate/core";

@NgModule({
  declarations: [
    NavbarComponent,
    FooterComponent,
    PageNotFoundComponent,
    AdminSelectionPageComponent,
    LeftSidebarComponent,
    ConfigSelectionPageComponent
  ],
  exports: [
    NavbarComponent,
    FooterComponent,
    LeftSidebarComponent
  ],
	imports: [
		CommonModule,
		MatIconModule,
		CoreRoutingModule,
		SharedModule,
		MatButtonModule,
		TranslateModule
	]
})
export class CoreModule { }
