import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {catchError, first, map} from 'rxjs/operators';
import {AuthService} from '@app/auth/services/auth.service';
import {Roles} from '@app/auth/enums/roles.enum';
import {TranslateService} from '@app/shared/services/translate.service';
import {of, Subscription} from 'rxjs';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
})

export class LoginComponent implements OnInit, OnDestroy {
  // __ = strings;

  loading = false;
  private subscriptions: Subscription = new Subscription();

  constructor(
    public translate: TranslateService,
    private authService: AuthService,
    private activatedRoute: ActivatedRoute,
    private router: Router
  ) {
  }

  ngOnInit() {
    this.activatedRoute.queryParams.pipe(first()).subscribe(params => {
      const {code} = params;
      let {returnUrl} = params;

      if (returnUrl) {
        localStorage.setItem('returnUrl', returnUrl);
      }

      if (code) {
        this.loading = true;
        this.subscriptions.add(
            this.authService.processLogin(code).pipe(
                first(),
                map(() => {
                  const currentUser = this.authService.currentDecodedUserValue;
                  returnUrl = localStorage.getItem('returnUrl');
                  if (returnUrl) {
                    return this.router.navigateByUrl(returnUrl);
                  } else if (currentUser.role !== Roles.SYSTEM_ADMIN) {
                      return this.router.navigateByUrl('projects');
                  } else {
                      return this.router.navigateByUrl('global-clients');
                  }
                }),
                catchError((error) => {
                    console.log(error);
                    return of(error.message);
                }),
              )
              .subscribe(() => this.loading = false)
        );
      }
    });
  }

  ngOnDestroy() {
      this.subscriptions.unsubscribe();
  }

  login() {
    this.loading = true;
    this.authService.authorize();
  }
}
