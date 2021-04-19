import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ConfigSelectionPageComponent } from './config-selection-page.component';

describe('ConfigSelectionPageComponent', () => {
  let component: ConfigSelectionPageComponent;
  let fixture: ComponentFixture<ConfigSelectionPageComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfigSelectionPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigSelectionPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
