import { Component} from '@angular/core';
import {User} from '@app/auth/models/user';
import {AuthService} from '@app/auth/services/auth.service';
import {Router} from '@angular/router';
import strings from '@i18n/strings.json';

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
  private __ = strings;

  private currentUser: User;
  private isAdmin: boolean;

  constructor(private authService: AuthService, private router: Router) {
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
    this.router.navigate(['/login']);
  }
}