import { Component, inject } from "@angular/core";
import { PaymentService } from "../../core/services/payment.service";
import { PaymentRequest } from "../../core/models/payment.request";
import { MatDialogRef } from "@angular/material/dialog";

@Component({
    selector:'app-payment',
    standalone:true,
    templateUrl: './payment.component.html',
    styleUrl: './payment.component.scss'
})
export class PaymentComponent {

    private paymentService = inject(PaymentService);


    constructor(private dialogRef: MatDialogRef<PaymentComponent>){}

    closeDialog(): void {
    this.dialogRef.close();
     }



    pay(){
        const request: PaymentRequest = {
            "name":"Rice",
            "amount":900,
            "quantity":40,
            "currency":"USD"
        }

        this.paymentService.checkout(request).subscribe({
            next: (response: any) => {
                window.location.href = response.data.sessionUrl;
            },
        error: (err) =>{
            console.error(err);
    }});

    }

}