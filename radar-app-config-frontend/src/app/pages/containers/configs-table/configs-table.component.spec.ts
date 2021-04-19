import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ConfigsTableComponent } from './configs-table.component';

describe('ConfigsTableComponent', () => {
  let component: ConfigsTableComponent;
  let fixture: ComponentFixture<ConfigsTableComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfigsTableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigsTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
