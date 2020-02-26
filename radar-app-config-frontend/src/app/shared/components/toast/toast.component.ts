import {Component, TemplateRef} from '@angular/core';
import {ToastService} from '@app/shared/services/toast.service';

@Component({
  selector: 'app-toasts',
  templateUrl: './toast.component.html',
  // template: '', // `
  //   <ngb-toast
  //     *ngFor="let toast of toastService.toasts"
  //     [header]="toast.headertext"
  //     [class]="toast.classname"
  //     [autohide]="toast.autohide"
  //     [delay]="toast.delay || 5000"
  //     (hide)="toastService.remove(toast)"
  //   >
  //       <ng-template [ngIf]="isTemplate(toast)" [ngIfElse]="text">
  //           <ng-template [ngTemplateOutlet]="toast.textOrTpl"></ng-template>
  //       </ng-template>
  //       <ng-template #text>{{ toast.textOrTpl }}</ng-template>
  //   </ngb-toast>
  // `,
  // tslint:disable-next-line:no-host-metadata-property
  host: {'[class.ngb-toasts]': 'true'}
})
export class ToastComponent {
  constructor(public toastService: ToastService) {}
  isTemplate(toast) { return toast.textOrTpl instanceof TemplateRef; }
}
