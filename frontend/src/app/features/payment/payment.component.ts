import { Component, inject, OnInit, signal } from "@angular/core";
import { PaymentService } from "../../core/services/payment.service";
import { PaymentRequest } from "../../core/models/payment.request";
import { MatDialogRef } from "@angular/material/dialog";
import { SubscriptionPlan } from "../../core/models/subscription-plan.model";

@Component({
    selector:'app-payment',
    standalone:true,
    templateUrl: './payment.component.html',
    styleUrl: './payment.component.scss'
})
export class PaymentComponent implements OnInit {

    private paymentService = inject(PaymentService);
    
    plans = signal<SubscriptionPlan[]>([]);

    constructor(private dialogRef: MatDialogRef<PaymentComponent>) {}

    ngOnInit(): void {
        this.loadPlans();
    }

    loadPlans(): void {
        this.paymentService.getPlans().subscribe({
            next: (response: any) => {
                this.plans.set(
                    response.data.sort(
                        (a: SubscriptionPlan, b: SubscriptionPlan) => a.price - b.price
                    )
                );
            },
            error: (err) => {
                console.error("Failed to load subscription plans:", err);
            }
        });
    }

    closeDialog(): void {
        this.dialogRef.close();
    }

    pay(plan: SubscriptionPlan){
        // Convert to cents for Stripe 
        const amountInCents = Math.round(plan.price * 100);

        const request: PaymentRequest = {
            "name": plan.name + " Plan",
            "amount": amountInCents,
            "quantity": 1,
            "currency": plan.currency || "USD",
            "planId": plan.id
        };

        this.paymentService.checkout(request).subscribe({
            next: (response: any) => {
                window.location.href = response.data.sessionUrl;
            },
            error: (err) =>{
                console.error(err);
            }
        });
    }
}