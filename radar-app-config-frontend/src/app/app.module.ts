import { BrowserModule } from '@angular/platform-browser';
import { NgModule} from '@angular/core';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AuthModule } from './auth/auth.module';
import { MatIconModule } from '@angular/material/icon';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CoreModule } from '@app/core/core.module';
import { SharedModule } from '@app/shared/shared.module';
import {AuthRoutingModule} from '@app/auth/auth-routing.module';
import {ProjectModule} from '@app/project/project.module';
import {CoreRoutingModule} from '@app/core/core-routing.module';
import {ProjectRoutingModule} from '@app/project/project-routing.module';
import {ClientRoutingModule} from '@app/client/client-routing.module';
import {ClientModule} from '@app/client/client.module';
import {ConfigRoutingModule} from '@app/config/config-routing.module';
import {ConfigModule} from '@app/config/config.module';
import {UserModule} from '@app/user/user.module';
import {UserRoutingModule} from '@app/user/user-routing.module';
import {MockModule} from '@app/mock/mock.module';
import {APP_BASE_HREF} from "@angular/common";
import {environment} from "@environments/environment";

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    SharedModule,
    MockModule,
    BrowserModule,
    BrowserAnimationsModule,
    MatIconModule,
    AuthModule,
    CoreModule,
    ProjectModule,
    ClientModule,
    UserModule,
    ConfigModule,
    ConfigRoutingModule,
    CoreRoutingModule,
    UserRoutingModule,
    ClientRoutingModule,
    ProjectRoutingModule,
    AuthRoutingModule,
    AppRoutingModule,
  ],
  providers: [
    { provide: APP_BASE_HREF, useValue: environment.baseURL },
  ],
  bootstrap: [AppComponent],

})
export class AppModule { }
