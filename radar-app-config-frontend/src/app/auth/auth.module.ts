import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './containers/login/login.component';
import { HttpClientModule} from '@angular/common/http';
import { AuthRoutingModule } from '@app/auth/auth-routing.module';

@NgModule({
  declarations: [LoginComponent],
  exports: [
    LoginComponent
  ],
  imports: [
    CommonModule,
    HttpClientModule,
    AuthRoutingModule
  ],
  providers: [],
})
export class AuthModule { }
