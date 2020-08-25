import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {ClientsComponent} from '@app/pages/containers/clients/clients.component';
import {GlobalClientsComponent} from '@app/pages/containers/global-clients/global-clients.component';
import {AuthGuard, AdminGuard} from '@app/auth/helpers';
import {ConfigsComponent} from "@app/pages/containers/configs/configs.component";
import {GlobalConfigsComponent} from "@app/pages/containers/global-configs/global-configs.component";
import {ProjectsComponent} from "@app/pages/containers/projects/projects.component";
import {UsersComponent} from "@app/pages/containers/users/users.component";

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
    {
        path: 'projects',
        component: ProjectsComponent,
        canActivate: [AuthGuard]
    },
    {
        path: 'users',
        component: UsersComponent,
        canActivate: [AuthGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class PagesRoutingModule { }
