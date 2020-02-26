import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {first} from 'rxjs/operators';
import {AuthService} from '@app/auth/services/auth.service';
import {Roles} from '@app/auth/enums/roles.enum';
import strings from '@i18n/strings.json';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  // styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  private __ = strings;

  private loading = false;

  constructor(
    private authService: AuthService,
    private activatedRoute: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.activatedRoute.queryParams.pipe(first()).subscribe(params => {
      const {code} = params;
      let {returnUrl} = params;

      if (returnUrl) {
        localStorage.setItem('returnUrl', returnUrl);
      }

      if (code) {
        this.loading = true;
        this.authService.processLogin(code).pipe(first())
          .subscribe(
            () => {
              const currentUser = this.authService.currentDecodedUserValue;
              returnUrl = localStorage.getItem('returnUrl');
              if (returnUrl) {
                this.router.navigateByUrl(returnUrl);
              } else {
                if (currentUser.role !== Roles.SYSTEM_ADMIN) {
                  this.router.navigateByUrl('projects');
                } else {
                  this.router.navigateByUrl('admin-main');
                }
              }
            },
            error => {
              console.log(error);
              // TODO Imp: logger
              // this.error = error;
              this.loading = false;
            }
          );
      }
    });
  }

  login() {
    this.loading = true;
    this.authService.authorize();
  }
}
