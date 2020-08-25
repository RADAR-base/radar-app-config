import { Injectable } from '@angular/core';
import { HttpRequest, HttpResponse, HttpHandler, HttpEvent, HttpInterceptor, HTTP_INTERCEPTORS } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { delay, mergeMap, materialize, dematerialize } from 'rxjs/operators';
// import { Role } from '@app/_models';
import {projects, clients, groups, participants, users} from '../mock-data';
import { environment as env} from '@environments/environment';
import { environment as envAdmin } from '@environments/environment.mock';
import { environment as envResearcher } from '@environments/environment.mock-researcher';
import {Roles} from '@app/auth/enums/roles.enum';

let environment;
if (env.envName === 'mock') {
  environment = envAdmin;
} else if (env.envName === 'mock-researcher') {
  environment = envResearcher;
}

@Injectable({
  providedIn: 'root'
})
export class MockInterceptor implements HttpInterceptor {

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let user; //  = users.users[0];
    const { url, method, headers, body } = request;
    return of(null)
      .pipe(mergeMap(handleRoute))
      .pipe(materialize())
      .pipe(delay(500))
      .pipe(dematerialize());

    function handleRoute() {
      switch (true) {
        case url.endsWith('/api/projects') && method === 'GET':
          return getProjects();
        case url.endsWith('/api/clients') && method === 'GET':
          return getClients();
        case url.match(/\/api\/projects\/[A-Za-z0-9_-]+\/clients\//) && method === 'GET':
          return getClientsByProjectId(getProjectIdFromUrl());
        case url.match(/\/api\/projects\/[A-Za-z0-9_-]+\/users\//) && method === 'GET':
          return getUsersByProjectId(getProjectIdFromUrl());
        case url.match(/\/api\/global\/config\//) && method === 'GET':
          return getConfigByClientId();
        case url.match(/\/api\/projects\/[A-Za-z0-9_-]+\/config\//) && method === 'GET':
          return getConfigByClientIdAndProjectId();
        case url.match(/\/api\/projects\/[A-Za-z0-9_-]+\/config\/[A-Za-z0-9_-]+/) && method === 'GET':
          return getConfigByClientIdAndProjectId();
        // tslint:disable-next-line:max-line-length
        case url.startsWith(`${environment.authAPI}/authorize?client_id=${environment.clientId}&response_type=code&redirect_uri=${environment.authCallback}`):
          generateCode();
          return null;
        // tslint:disable-next-line:max-line-length
        // case url.startsWith(`${environment.authAPI}/researcher-authorize?client_id=${environment.clientId}&response_type=code&redirect_uri=${environment.authCallback}`):
        //   generateCode('researcher');
        //   return null;
        case url.startsWith(`${environment.authAPI}/token`):
          return generateToken();
        // case url.startsWith(`${environment.authAPI}/researcher-token`):
        //   return generateToken('researcher');
        default:
          return next.handle(request);
      }
    }

    // route functions
    function generateToken() {
      if (environment.envName === 'mock') {
        user = users.users[0];
      } else if (environment.envName === 'mock-researcher') {
        user = users.users[1];
      }
      // if (role === 'researcher') {
      //   user = users.users[1];
      // }
      return ok(user);
    }

    function generateCode() {
      const code = environment.fakeCode.code;
      window.location.href = `${environment.authCallback}?code=${code}`;
    }

    function getProjects() {
      if (!isLoggedIn()) {
        return unauthorized();
      }
      // if (isAdmin()) {
      //   console.log(888);
      //   const projectsList = projects.projects.map(p => p.data);
      //   return ok({projects: projectsList});
      // }
      // TODO
      if (environment.envName === 'mock') {
        user = users.users[0];
      } else if (environment.envName === 'mock-researcher') {
        user = users.users[1];
      }
      if (user.projects) {
        const projectsLists = user.projects.map(projectId => projects.projects.find(p => p.data.id === projectId));
        return ok({projects: projectsLists.map(p => p.data)});

        // return ok(user.projects.map(projectId => projects.projects.find(p => p.data.id === projectId)));
      }
      const projectsList = projects.projects.map(p => p.data);
      return ok({projects: projectsList});
      // return ok(projects);

    }

    function getClients() {
      if (isAdmin()) { return ok(clients); }
      // TODO
      return ok(null); // researcherClients);
    }

    function getClientsByProjectId(projectId) {
      // console.log(projectId);
      const selectedProject = projects.projects.filter(p => p.data.id == projectId);
      // console.log(selectedProject);
      const clientsOfSelectedProject = selectedProject[0].clients;
      // console.log(clientsOfSelectedProject);
      const clientsList = clientsOfSelectedProject.map(clientId => clients.clients.filter(c => c.clientId == clientId)[0]);
      // console.log(clientsList);
      // const clientsList =
      return ok({clients: clientsList});
    }

    function getUsersByProjectId(projectId) {
      const selectedProject = projects.projects.filter(p => p.data.projectName == projectId);
      const usersOfSelectedProject = selectedProject[0].participants;
      const usersList = usersOfSelectedProject.map(userId => participants.participants.filter(u => u.id == userId)[0]);
      // const clientsList =
      return ok({users: usersList});
    }

    function getConfigByClientId() {
      if (!isLoggedIn()) { return unauthorized(); }
      if (!isAdmin()) { return unauthorized(); }
      const config = {
        clientId: 'fakeClientId',
        scope: 'global',
        config: [
          {name: 'plugins', value: 'A', scope: 'global'}
        ]
      };
      // const config = globalConfigs.find(c => c.clientId === clientIdFromUrl());
      return ok(config);
    }

    function getConfigByClientIdAndProjectId() {
      if (!isLoggedIn()) { return unauthorized(); }
      // const config = nonGlobalConfigs.find(c => c.clientId === clientIdFromUrl() && true);
      const config = {
        clientId: 'fakeClientId',
        scope: 'project.fakeProjectId',
        config: [
          {name: 'plugins', value: 'A B'},
          {name: 'plugins-a', value: 'C D'}
        ],
        defaults: [
          {name: 'plugins', value: 'A', scope: 'global'}
        ]
      };
      return ok(config);
    }

    // helper functions
    // tslint:disable-next-line:no-shadowed-variable
    function ok(body) {
      return of(new HttpResponse({ status: 200, body }));
    }

    function unauthorized() {
      return throwError({ status: 401, error: { message: 'unauthorized' } });
    }

    function isLoggedIn() {
      const authHeader = headers.get('Authorization') || '';
      // tslint:disable-next-line:max-line-length
      return authHeader.startsWith(`Bearer ${environment.fakeJwtToken.token}`);
    }

    function isAdmin() {
      return isLoggedIn() && currentUser().role === Roles.SYSTEM_ADMIN;
    }

    function currentUser() {
      if (!isLoggedIn()) { return; }
      return {role: Roles.SYSTEM_ADMIN};
    }

    function clientIdFromUrl() {
      const urlParts = url.split('/');
      // tslint:disable-next-line: radix
      return (urlParts[urlParts.length - 1]);
    }

    function getProjectIdFromUrl() {
      const urlParts = url.split('/');
      // tslint:disable-next-line: radix
      return (urlParts[urlParts.length - 3]);
    }

    function getProjectIdAndClientIdFromUrl() {
      const urlParts = url.split('/');
      // tslint:disable-next-line: radix
      return (urlParts[urlParts.length - 3]);
    }
  }
}

export const mockBackendProvider = {
  // use fake backend in place of Http service for backend-less development
  provide: HTTP_INTERCEPTORS,
  useClass: MockInterceptor,
  multi: true
};
