import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UsersComponent } from './containers/users/users.component';
import { CoreModule } from '@app/core/core.module';
import { SharedModule } from '@app/shared/shared.module';
import { RouterModule } from '@angular/router';

@NgModule({
  declarations: [UsersComponent],
  imports: [
    CommonModule,
    CoreModule,
    RouterModule,
    SharedModule
  ]
})
export class UserModule { }
