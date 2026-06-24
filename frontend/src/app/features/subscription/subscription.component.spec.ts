import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubscriptionsComponent  } from './subscription.component';

describe('Subscription', () => {
  let component: SubscriptionsComponent ;
  let fixture: ComponentFixture<SubscriptionsComponent >;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SubscriptionsComponent ],
    }).compileComponents();

    fixture = TestBed.createComponent(SubscriptionsComponent );
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
