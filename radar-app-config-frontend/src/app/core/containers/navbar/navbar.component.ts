import { Component} from '@angular/core';
import {User} from '@app/auth/models/user';
import {AuthService} from '@app/auth/services/auth.service';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateService} from "@ngx-translate/core";
// import strings from '@i18n/strings.json';

/**
 * Navbar component
 */
@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent {

  /**
   * @ignore
   */
  // __ = strings;

  currentUser: User;
  isAdmin: boolean;
  isNavMenuActive: boolean = false;

  navbarOpen = false;

  constructor(
    private authService: AuthService,
    public router: Router,
    public translateService: TranslateService,
  ){
    this.authService.currentUser.subscribe(() => {
      this.currentUser = this.authService.currentDecodedUserValue;
      this.isAdmin = this.authService.isAdmin;
    });
  }

  /**
   * Logout click handler
   */
  logout() {
    this.authService.logout();
    this.currentUser = null;
    localStorage.removeItem('returnUrl');
    this.isNavMenuActive = false;
    this.router.navigate(['/login']);
  }

  toggleNavbar() {
    this.navbarOpen = !this.navbarOpen;
  }

  closeNavbar(){
    this.navbarOpen = false;
  }

  openHelp() {
    this.translateService.get('general.helpUrl')
      .subscribe({
        next: value => window.open(value, '_blank'),
      })
  }
}
