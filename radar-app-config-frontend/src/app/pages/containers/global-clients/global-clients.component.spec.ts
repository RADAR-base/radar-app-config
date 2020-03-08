import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GlobalClientsComponent } from './global-clients.component';

describe('GlobalClientsComponent', () => {
  let component: GlobalClientsComponent;
  let fixture: ComponentFixture<GlobalClientsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GlobalClientsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GlobalClientsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
