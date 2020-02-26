import { Component } from '@angular/core';
import strings from '@i18n/strings.json';

/**
 * Footer component
 */
@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})

export class FooterComponent {
  /**
   * @ignore
   */
  private __ = strings;
}
