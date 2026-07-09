export interface SubscriptionPlan {
  id: string;
  name: string;
  price: number;
  currency: string;
  billingCycle: string;
  subtitle: string;
  isPopular: boolean;
  planClass: string;
  features: string[];
}