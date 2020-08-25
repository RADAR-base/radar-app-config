import {Injectable, TemplateRef} from '@angular/core';

/**
 * Toast Service
 */
@Injectable({
  providedIn: 'root'
})
export class ToastService {
  toasts: any[] = [];

  show(textOrTpl: string | TemplateRef<any>, options: any = {}) {
    this.toasts.push({ textOrTpl, ...options });
  }

  remove(toast) {
    this.toasts = this.toasts.filter(t => t !== toast);
  }

  showSuccess(message: string,
              args = {
                classname: 'bg-success text-light',
                delay: 2000 ,
                autohide: true
              }) {
    this.show(message, args);
  }

  showError(
    message: string,
    args = {
      classname: 'bg-danger text-light',
      delay: 2000 ,
      autohide: true,
    }) {
    this.show(message, args);
  }

  // showCustomToast(customTpl) {
  //   this.show(customTpl, {
  //     classname: 'bg-info text-light',
  //     delay: 3000,
  //     autohide: true
  //   });
  // }
}
