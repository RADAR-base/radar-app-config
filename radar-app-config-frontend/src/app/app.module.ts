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
import {CoreRoutingModule} from '@app/core/core-routing.module';
import {MockModule} from '@app/mock/mock.module';
import {APP_BASE_HREF} from "@angular/common";
import {environment} from "@environments/environment";
import {PagesModule} from "@app/pages/pages.module";
import {PagesRoutingModule} from "@app/pages/pages-routing.module";

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    SharedModule,
    PagesModule,
    MockModule,
    BrowserModule,
    BrowserAnimationsModule,
    MatIconModule,
    AuthModule,
    CoreModule,
    CoreRoutingModule,
      PagesRoutingModule,
    AuthRoutingModule,
    AppRoutingModule
  ],
  providers: [
    { provide: APP_BASE_HREF, useValue: environment.baseURL },
  ],
  bootstrap: [AppComponent],

})
export class AppModule { }
