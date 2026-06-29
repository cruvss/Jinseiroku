export interface Task {
  id?: string;
  titleEncrypted: string;
  descriptionEncrypted?: string;
  category: string;
  isRecurring: boolean;
  cycleType?: 'DAYS' | 'WEEKS' | 'MONTHS' | 'YEARS';
  cycleInterval?: number;
  dueDate?: string; // ISO date string
  leadTimeDays?: number;
  status?: 'pending' | 'completed';
  linkedDocumentId?: string;
  createdAt?: string;
  updatedAt?: string;

  // Local decrypted fields
  decryptedTitle?: string;
  decryptedDescription?: string;
  decryptedNotes?: string;
}

export interface TaskCompletion {
  id: string;
  taskId: string;
  completedAt: string;
  notesEncrypted?: string;
  decryptedNotes?: string;
}