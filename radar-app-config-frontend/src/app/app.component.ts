import {Component} from '@angular/core';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import { environment } from "@environments/environment";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  // styleUrls: ['./app.component.scss']
})
export class AppComponent {
  constructor(
      private matIconRegistry: MatIconRegistry,
      private domSanitizer: DomSanitizer
  ) {
    matIconRegistry.addSvgIconResolver((name, namespace) =>
      domSanitizer.bypassSecurityTrustResourceUrl(`${environment.baseURL}/assets/icons/${name}.svg`)
    );
  }
}
