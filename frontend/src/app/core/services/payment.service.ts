import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { PaymentRequest } from "../models/payment.request";

@Injectable({providedIn:'root'})
export class PaymentService {
    private http = inject(HttpClient);
    private url  = `${environment.apiUrl}/payment/checkout`;

    checkout(request: PaymentRequest){
        return this.http.post(this.url,request);
    }

}