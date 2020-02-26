import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {ConfigsComponent} from '@app/config/containers/configs/configs.component';
import {GlobalConfigsComponent} from '@app/config/containers/global-configs/global-configs.component';
import {AdminGuard, AuthGuard} from '@app/auth/helpers';

const routes: Routes = [
  {
    path: 'configs',
    component: ConfigsComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'global-configs',
    component: GlobalConfigsComponent,
    canActivate: [AuthGuard, AdminGuard]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ConfigRoutingModule { }
