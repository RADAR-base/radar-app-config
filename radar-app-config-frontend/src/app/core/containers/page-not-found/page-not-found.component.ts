import { Component} from '@angular/core';
import {TranslateService} from "@app/shared/services/translate.service";

/**
 * Page Not Found component
 */
@Component({
  selector: 'app-page-not-found',
  templateUrl: './page-not-found.component.html',
})

export class PageNotFoundComponent {
  constructor(public translate: TranslateService) {}
}
