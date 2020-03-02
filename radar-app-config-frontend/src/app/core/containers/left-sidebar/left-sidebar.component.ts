import {Component, HostBinding, Input} from '@angular/core';
import strings from '@i18n/strings.json';

@Component({
  selector: 'app-left-sidebar',
  templateUrl: './left-sidebar.component.html',
  // styleUrls: ['./left-sidebar.component.scss'],
})


export class LeftSidebarComponent{
  __ = strings;
  @Input() backButton: {routerLink: any, queryParams: any, name: string};
  @HostBinding('class') class = 'container-left';

  constructor() {}
}
