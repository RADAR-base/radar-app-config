import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ClientsComponent } from './containers/clients/clients.component';
import {CoreModule} from '@app/core/core.module';
import {RouterModule} from '@angular/router';
import {SharedModule} from '@app/shared/shared.module';
import { GlobalClientsComponent } from './containers/global-clients/global-clients.component';
import {NgbDropdownModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
  declarations: [ClientsComponent, GlobalClientsComponent],
    imports: [
        CommonModule,
        CoreModule,
        RouterModule,
        SharedModule,
        NgbDropdownModule
    ]
})
export class ClientModule { }
