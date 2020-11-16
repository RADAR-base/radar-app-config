import { Injectable } from '@angular/core';
import {TranslateService as Translate} from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class TranslateService {

  constructor(public translate: Translate) {
    translate.setDefaultLang('en');
  }
}
