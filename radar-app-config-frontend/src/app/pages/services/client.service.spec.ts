import { TestBed } from '@angular/core/testing';

import { ClientService } from './client.service';

describe('ClientService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ClientService = TestBed.inject(ClientService);
    expect(service).toBeTruthy();
  });
});
