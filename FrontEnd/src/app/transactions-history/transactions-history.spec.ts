import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TransactionsHistory } from './transactions-history';

describe('TransactionsHistory', () => {
  let component: TransactionsHistory;
  let fixture: ComponentFixture<TransactionsHistory>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransactionsHistory]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TransactionsHistory);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
