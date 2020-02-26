import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {AdminSelectionPageComponent} from '@app/core/containers/admin-selection-page/admin-selection-page.component';
import {ConfigSelectionPageComponent} from '@app/core/containers/config-selection-page/config-selection-page.component';
import {AdminGuard, AuthGuard} from '@app/auth/helpers';

const routes: Routes = [
  {
    path: 'admin-main',
    component: AdminSelectionPageComponent,
    canActivate: [AuthGuard, AdminGuard]
  },
  {
    path: 'config-selection',
    component: ConfigSelectionPageComponent,
    canActivate: [AuthGuard]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CoreRoutingModule {}
