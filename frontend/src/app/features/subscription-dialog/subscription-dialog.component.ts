import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, provideNativeDateAdapter } from '@angular/material/core';
import { Subscription } from '../../core/models/subscription.model';
import { SubscriptionDialogData } from '../../core/models/subscriptoin-dialog.model';
import currencyCodes from 'currency-codes';

@Component({
  selector: 'app-subscription-dialog',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  providers: [
    provideNativeDateAdapter() 
  ],
  templateUrl: './subscription-dialog.component.html',
  styleUrl: './subscription-dialog.component.scss',
})
export class SubscriptionDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<SubscriptionDialogComponent>);
  public data: SubscriptionDialogData = inject(MAT_DIALOG_DATA) || {};


  currencies = [
    { code: 'JPY', symbol: '¥' },
    { code: 'NPR', symbol: 'रु' },
    { code: 'USD', symbol: '$' },
    { code: 'GBP', symbol: '£' },
    { code: 'EUR', symbol: '€' }
  ];

  form = this.fb.group({
    name: ['', Validators.required],
    cost: [0, [Validators.required, Validators.min(0)]],
    currency: ['NPR', Validators.required],
    billingCycle: ['MONTHLY', Validators.required],
    nextBillingDate: ['', Validators.required],
    status: ['ACTIVE', Validators.required],
  });

  ngOnInit() {
    if (this.data?.subscription) {
      const s = this.data.subscription;
      this.form.patchValue({
        name: s.name,
        cost: s.cost,
        currency: s.currency,
        billingCycle: s.billingCycle,
        nextBillingDate: s.nextBillingDate,
        status: s.status
      });
    } else if (this.data?.inboxText) {
      this.form.patchValue({ name: this.data.inboxText });
    }
  }

  submit() {
    if (this.form.valid) {
      const formValue = this.form.value;
      const result: Subscription = {
        ...this.data?.subscription,
        name: formValue.name!,
        cost: formValue.cost!,
        currency: formValue.currency!,
        billingCycle: formValue.billingCycle! as any,
        nextBillingDate: formValue.nextBillingDate!,
        status: formValue.status! as any,
      };
      this.dialogRef.close(result);
    }
  }
}
