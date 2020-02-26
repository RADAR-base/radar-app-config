// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  envName: 'dev',
  production: false,

  authAPI: 'http://localhost:8080/managementportal/oauth',
  clientId: 'appconfig_frontend',
  authCallback: 'http://localhost:4200/appconfig/login',
  appUrl: 'http://localhost:4200',
  baseURL: 'http://localhost:8085/upload/api',

};
