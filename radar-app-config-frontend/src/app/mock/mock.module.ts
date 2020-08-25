import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {mockBackendProvider} from '@app/mock/services/mock.service';
import {environment} from '@environments/environment';

const extraProviders = (environment.envName === 'mock' || environment.envName === 'mock-researcher') ? [mockBackendProvider] : [];

@NgModule({
  declarations: [],
  imports: [
    CommonModule
  ],
  providers: [
    ...extraProviders
  ],
})
export class MockModule { }
