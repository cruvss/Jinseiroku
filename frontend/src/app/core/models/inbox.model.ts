export interface InboxItem {
  id: string;
  contentType: string;
  textContentEncrypted?: string;
  encryptedDek?: string;
  fileSizeBytes?: number;
  mimeType?: string;
  capturedAt: string;
  decryptedText?: string;
}
