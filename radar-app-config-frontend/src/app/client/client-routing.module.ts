import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {ClientsComponent} from '@app/client/containers/clients/clients.component';
import {GlobalClientsComponent} from '@app/client/containers/global-clients/global-clients.component';
import {AuthGuard, AdminGuard} from '@app/auth/helpers';

const routes: Routes = [
  {
    path: 'clients',
    component: ClientsComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'global-clients',
    component: GlobalClientsComponent,
    canActivate: [AuthGuard, AdminGuard]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ClientRoutingModule { }
