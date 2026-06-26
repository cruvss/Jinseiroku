import { HttpClient } from "@angular/common/http";
import { inject, Injectable, signal,computed } from "@angular/core";
import { environment } from "../../../environments/environment";
import { Observable, map } from "rxjs";
import { Subscription } from "../models/subscription.model";

@Injectable({providedIn:'root'})
export class SubscriptionService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/subscriptions`;

    exchangeRates: Record<string,number> = {
    NPR: 1,
    USD: 150.40,
    EUR: 171.26,
    GBP: 198.67,
    JPY: 0.93
    };
    

    subscriptions = signal<Subscription[]>([]);

    loadSubscriptions() {
        this.getSubscriptions().subscribe(data => {
            this.subscriptions.set(data);
        });
        }
    
    getSubscriptions():Observable<Subscription[]> {
        return this.http.get<{data: Subscription[]}>(this.apiUrl).pipe(map(res => res.data));
    }

    createSubscription(sub: Subscription): Observable<Subscription>{
        return this.http.post<{data: Subscription}>(this.apiUrl, sub).pipe(map(res => res.data));
    }

    updateSubscription(id:string, sub: Subscription): Observable<Subscription> {
        return this.http.put<{data: Subscription}>(`${this.apiUrl}/${id}`, sub).pipe(map(res => res.data));
    }

    deleteSubscription(id:string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    totalMonthly = computed(() => {
    return this.subscriptions().reduce((total, sub) => {
      const cost = sub.cost;
      if (sub.status !== 'ACTIVE') return total;

      const costInNPR = sub.cost * (this.exchangeRates[sub.currency]);

      switch (sub.billingCycle) {
        case 'MONTHLY': return total + costInNPR;
        case 'YEARLY': return total + (costInNPR / 12);
        case 'WEEKLY': return total + (costInNPR * 4.333);
        case 'FORTNIGHTLY': return total + (costInNPR * 2.166);
        case 'QUARTERLY': return total + (costInNPR / 3);
        case 'SEMI-YEARLY': return total + (costInNPR / 6);
        default: return total;
      }
        }, 0);
    });


}