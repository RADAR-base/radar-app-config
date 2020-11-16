import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigsTableComponent } from './configs-table.component';

describe('ConfigsTableComponent', () => {
  let component: ConfigsTableComponent;
  let fixture: ComponentFixture<ConfigsTableComponent>;

  beforeEach(async(() => {
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
