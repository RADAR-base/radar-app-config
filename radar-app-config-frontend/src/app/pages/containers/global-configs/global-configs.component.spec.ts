import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GlobalConfigsComponent } from './global-configs.component';

describe('GlobalConfigsComponent', () => {
  let component: GlobalConfigsComponent;
  let fixture: ComponentFixture<GlobalConfigsComponent>;

  beforeEach(async(() => {
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
