import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { GlobalConfigsComponent } from './global-configs.component';

describe('GlobalConfigsComponent', () => {
  let component: GlobalConfigsComponent;
  let fixture: ComponentFixture<GlobalConfigsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ GlobalConfigsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GlobalConfigsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
