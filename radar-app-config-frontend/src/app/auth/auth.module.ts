import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './containers/login/login.component';
import { HttpClientModule} from '@angular/common/http';
import { AuthRoutingModule } from '@app/auth/auth-routing.module';
import {TranslateModule} from "@ngx-translate/core";

@NgModule({
  declarations: [LoginComponent],
  exports: [
    LoginComponent
  ],
	imports: [
		CommonModule,
		HttpClientModule,
		AuthRoutingModule,
		TranslateModule
	],
  providers: [],
})
export class AuthModule { }
