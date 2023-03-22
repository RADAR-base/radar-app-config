import {Component, HostBinding, TemplateRef} from '@angular/core';
import {ToastService} from '@app/shared/services/toast.service';

@Component({
  selector: 'app-toasts',
  templateUrl: './toast.component.html',
})
export class ToastComponent {
  @HostBinding('class.ngb-toasts') showToasts = true;

  constructor(public toastService: ToastService) {}

  isTemplate(toast) { return toast.textOrTpl instanceof TemplateRef; }
}
