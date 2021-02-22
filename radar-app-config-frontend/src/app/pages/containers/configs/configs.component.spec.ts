import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ConfigsComponent } from './configs.component';

describe('ConfigsComponent', () => {
  let component: ConfigsComponent;
  let fixture: ComponentFixture<ConfigsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfigsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
