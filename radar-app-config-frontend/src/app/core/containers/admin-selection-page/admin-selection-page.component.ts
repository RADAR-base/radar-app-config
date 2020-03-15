import { Component } from '@angular/core';
import {TranslateService} from "@app/shared/services/translate.service";
// import strings from '@i18n/strings.json';

/**
 * Admin Selection Page Component
 */
@Component({
  selector: 'app-admin-selection-page',
  templateUrl: './admin-selection-page.component.html',
  // styleUrls: ['./admin-selection-page.component.scss']
})
export class AdminSelectionPageComponent {
  // __ = strings;
  constructor(public translate: TranslateService) {}
}
