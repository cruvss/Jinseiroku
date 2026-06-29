export interface TimelineEvent {
  id?: string;
  titleEncrypted: string;
  descriptionEncrypted?: string;
  eventDate: string;
  endDate?: string;
  category: string;
  linkedDocumentIds?: string[];
  createdAt?: string;
  updatedAt?: string;

  // Decrypted fields for UI binding
  decryptedTitle?: string;
  decryptedDescription?: string;
}

export interface TimelineDialogData {
  event?: TimelineEvent;
  inboxText?: string;
}