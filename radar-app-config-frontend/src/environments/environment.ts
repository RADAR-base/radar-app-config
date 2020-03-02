// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  envName: 'dev',
  production: false,
  baseURL: 'http://localhost:4200/',
  authAPI: 'http://localhost:8080/managementportal/oauth',
  authCallback: 'http://localhost:4200/login',
  clientId: 'appconfig_frontend',
  backendUrl: '/api',
};
