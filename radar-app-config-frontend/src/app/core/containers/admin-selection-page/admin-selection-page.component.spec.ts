import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { AdminSelectionPageComponent } from './admin-selection-page.component';

describe('AdminSelectionPageComponent', () => {
  let component: AdminSelectionPageComponent;
  let fixture: ComponentFixture<AdminSelectionPageComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AdminSelectionPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminSelectionPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
