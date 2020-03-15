import { Component } from '@angular/core';
import {TranslateService} from "@app/shared/services/translate.service";

/**
 * Footer component
 */
@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})

export class FooterComponent {
  constructor(public translate: TranslateService) {}
}
