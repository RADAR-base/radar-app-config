import { NgModule } from '@angular/core';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ToastComponent } from './components/toast/toast.component';
import { DropDownComponent } from './components/drop-down/drop-down.component';
import { JwtInterceptor } from '@app/shared/helpers/jwt.interceptor';
import { ErrorInterceptor } from '@app/shared/helpers/error.interceptor';
import {MatSelectModule} from "@angular/material/select";
import {MatInputModule} from "@angular/material/input";

@NgModule({
  declarations: [ToastComponent, DropDownComponent],
  exports: [ToastComponent, DropDownComponent],
  imports: [
    CommonModule,
    NgbModule,
    MatIconModule,
    MatSelectModule,
    MatInputModule,
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
  ],
})
export class SharedModule { }
