import { TestBed } from '@angular/core/testing';

import { MockInterceptor } from './mock.service';

describe('MockInterceptor', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: MockInterceptor = TestBed.inject(MockInterceptor);
    expect(service).toBeTruthy();
  });
});
