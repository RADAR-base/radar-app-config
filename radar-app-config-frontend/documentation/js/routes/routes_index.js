var ROUTES_INDEX = {"name":"<root>","kind":"module","className":"AppModule","children":[{"name":"routes","filename":"src/app/app-routing.module.ts","module":"AppRoutingModule","children":[{"path":"","redirectTo":"/login","pathMatch":"full"},{"path":"**","component":"PageNotFoundComponent"}],"kind":"module"},{"name":"routes","filename":"src/app/auth/auth-routing.module.ts","module":"AuthRoutingModule","children":[{"path":"login","component":"LoginComponent"}],"kind":"module"},{"name":"routes","filename":"src/app/core/core-routing.module.ts","module":"CoreRoutingModule","children":[],"kind":"module"},{"name":"routes","filename":"src/app/pages/pages-routing.module.ts","module":"PagesRoutingModule","children":[{"path":"clients","component":"ClientsComponent","canActivate":["AuthGuard"]},{"path":"global-clients","component":"GlobalClientsComponent","canActivate":["AuthGuard","AdminGuard"]},{"path":"configs","component":"ConfigsComponent","canActivate":["AuthGuard"]},{"path":"global-configs","component":"GlobalConfigsComponent","canActivate":["AuthGuard","AdminGuard"]},{"path":"projects","component":"ProjectsComponent","canActivate":["AuthGuard"]},{"path":"users","component":"UsersComponent","canActivate":["AuthGuard"]}],"kind":"module"}]}
