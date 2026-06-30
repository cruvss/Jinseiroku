export interface ScheduledNotification {
  id: string;
  reminderRuleId: string;
  userId: string;
  scheduledFor: string;
  channel: string;
  title: string;
  body: string;
  status: string;
  sentAt?: string;
}