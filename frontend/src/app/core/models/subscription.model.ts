export interface Subscription {
    id: string;
    name: string;
    cost: number;
    currency: string;
    billingCycle: 'WEEKLY' | 'FORTNIGHTLY' | 'MONTHLY' | 'QUARTERLY' | 'SEMI-YEARLY' | 'YEARLY';
    nextBillingDate?: string;
    status: 'ACTIVE' | 'CANCELLED' | 'PAUSED';
    linkedDocumentId: string;
}