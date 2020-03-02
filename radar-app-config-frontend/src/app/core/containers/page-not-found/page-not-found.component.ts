import { Component} from '@angular/core';
import strings from '@i18n/strings.json';

/**
 * Page Not Found component
 */
@Component({
  selector: 'app-page-not-found',
  templateUrl: './page-not-found.component.html',
  // styleUrls: ['./page-not-found.component.scss']
})
export class PageNotFoundComponent {
  /**
   * @ignore
   */
  __ = strings;
}
