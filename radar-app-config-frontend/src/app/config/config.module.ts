import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfigsComponent } from './containers/configs/configs.component';
import { GlobalConfigsComponent } from './containers/global-configs/global-configs.component';
import {RouterModule} from '@angular/router';
import {CoreModule} from '@app/core/core.module';
import {SharedModule} from '@app/shared/shared.module';
import { ConfigsTableComponent } from './containers/configs-table/configs-table.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material';
import {MatButtonModule} from "@angular/material/button";
import {NgbDropdownModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
  declarations: [ConfigsComponent, GlobalConfigsComponent, ConfigsTableComponent],
    imports: [
        CommonModule,
        RouterModule,
        CoreModule,
        SharedModule,
        FormsModule,
        ReactiveFormsModule,
        MatIconModule,
        MatButtonModule,
        NgbDropdownModule
    ]
})
export class ConfigModule { }
