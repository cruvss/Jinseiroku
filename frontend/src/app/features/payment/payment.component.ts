import { Component, inject, OnInit, signal } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { environment } from "../../../environments/environment";
import { PaymentService } from "../../core/services/payment.service";
import { PaymentRequest } from "../../core/models/payment.request";
import { MatDialogRef } from "@angular/material/dialog";
import { SubscriptionPlan } from "../../core/models/subscription-plan.model";

@Component({
    selector: 'app-payment',
    standalone: true,
    templateUrl: './payment.component.html',
    styleUrl: './payment.component.scss'
})
export class PaymentComponent implements OnInit {
    private http = inject(HttpClient);
    private paymentService = inject(PaymentService);
    private dialogRef = inject(MatDialogRef<PaymentComponent>);

    plans = signal<SubscriptionPlan[]>([]);
    currentPlanId = signal<string | null>(null);  

    ngOnInit(): void {
        this.loadPlans();
        this.loadUserProfile();
    }

    loadPlans(): void {
        this.paymentService.getPlans().subscribe({
            next: (response: any) => {
                this.plans.set(response.data.sort((a: any, b: any) => a.price - b.price));
            },
            error: (err) => {
                console.error("Failed to load plans:", err);
            }
        });
    }

    getCurrentPlanPrice(): number {
        const activePlan = this.plans().find(p => p.id === this.currentPlanId());
        return activePlan ? activePlan.price : 0; 
    }

    loadUserProfile(): void {
        this.http.get<any>(`${environment.apiUrl}/auth/me`).subscribe({
            next: (response: any) => {
                this.currentPlanId.set(response.data?.subscriptionPlanId);
            },
            error: (err) => {
                console.error("Failed to fetch user profile:", err);
            }
        });
    }

    closeDialog(): void {
        this.dialogRef.close();
    }

    pay(plan: SubscriptionPlan) {
        const amountInCents = Math.round(plan.price * 100);
        const request: PaymentRequest = {
            "name": plan.name + " Plan",
            "amount": amountInCents,
            "quantity": 1,
            "currency": plan.currency || "USD",
            "planId": plan.id
        } as any;

        this.paymentService.checkout(request).subscribe({
            next: (response: any) => {
                window.location.href = response.data.sessionUrl;
            },
            error: (err) => {
                console.error(err);
            }
        });
    }
}