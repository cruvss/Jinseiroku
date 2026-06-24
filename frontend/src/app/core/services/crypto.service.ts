import { Injectable } from '@angular/core';
import { argon2id } from 'hash-wasm';

@Injectable({
  providedIn: 'root'
})
export class CryptoService {
  private kek: CryptoKey | null = null;

  // Derive KEK using Argon2id and import it as an AES-KW (AES Key Wrap) Key
  async initializeSession(password: string, saltHex: string): Promise<void> {
    const saltBytes = this.hexToUint8Array(saltHex);
    const passwordBytes = new TextEncoder().encode(password);
    
    // Hash password with Argon2id WASM engine
    const rawKekBytes = await argon2id({
      password: passwordBytes,
      salt: saltBytes,
      iterations: 3,
      memorySize: 65536,
      parallelism: 4,
      hashLength: 32, // 256-bit AES key
      outputType: 'binary'
    });

    // Import the resulting raw 32 bytes as a CryptoKey for Key Wrapping
    this.kek = await window.crypto.subtle.importKey(
      'raw',
      rawKekBytes.buffer.slice(rawKekBytes.byteOffset, rawKekBytes.byteOffset + rawKekBytes.byteLength) as ArrayBuffer,
      { name: 'AES-KW' },
      false,
      ['wrapKey', 'unwrapKey']
    );
  }

  hasActiveSession(): boolean {
    return this.kek !== null;
  }

  clearSession(): void {
    this.kek = null;
  }

  // Generate a random Data Encryption Key (DEK)
  async generateDEK(): Promise<CryptoKey> {
    return window.crypto.subtle.generateKey(
      { name: 'AES-GCM', length: 256 },
      true, // must be extractable so we can wrap it
      ['encrypt', 'decrypt']
    );
  }

  // Encrypt file bytes using the DEK with AES-GCM
  async encryptFile(file: BufferSource, dek: CryptoKey): Promise<{ ciphertext: ArrayBuffer; iv: Uint8Array }> {
    const iv = window.crypto.getRandomValues(new Uint8Array(12)); // 96-bit IV
    const ciphertext = await window.crypto.subtle.encrypt(
      { name: 'AES-GCM', iv },
      dek,
      file
    );
    return { ciphertext, iv };
  }

  // Decrypt file bytes using the DEK with AES-GCM
  async decryptFile(ciphertext: BufferSource, dek: CryptoKey, iv: BufferSource): Promise<ArrayBuffer> {
    return window.crypto.subtle.decrypt(
      { name: 'AES-GCM', iv },
      dek,
      ciphertext
    );
  }

  // Wrap DEK with the session KEK (using AES-KW)
  async wrapDEK(dek: CryptoKey): Promise<string> {
    if (!this.kek) {
      throw new Error('No active vault session (KEK not derived)');
    }
    const wrappedArrayBuffer = await window.crypto.subtle.wrapKey(
      'raw',
      dek,
      this.kek,
      'AES-KW'
    );
    return this.arrayBufferToBase64(wrappedArrayBuffer);
  }

  // Unwrap DEK with the session KEK (using AES-KW)
  async unwrapDEK(wrappedDekBase64: string): Promise<CryptoKey> {
    if (!this.kek) {
      throw new Error('No active vault session (KEK not derived)');
    }
    const wrappedBuffer = this.base64ToArrayBuffer(wrappedDekBase64);
    return window.crypto.subtle.unwrapKey(
      'raw',
      wrappedBuffer,
      this.kek,
      'AES-KW',
      { name: 'AES-GCM', length: 256 },
      true,
      ['encrypt', 'decrypt']
    );
  }

  // Helper utility to encrypt metadata fields (like file name or notes)
  async encryptText(text: string, dek: CryptoKey): Promise<{ ciphertext: string; iv: string }> {
    const enc = new TextEncoder();
    const result = await this.encryptFile(enc.encode(text), dek);
    return {
      ciphertext: this.arrayBufferToBase64(result.ciphertext),
      iv: this.uint8ArrayToBase64(result.iv)
    };
  }

  // Helper utility to decrypt metadata fields
  async decryptText(ciphertextBase64: string, dek: CryptoKey, ivBase64: string): Promise<string> {
    const cipherBuffer = this.base64ToArrayBuffer(ciphertextBase64);
    const ivBytes = this.base64ToUint8Array(ivBase64);
    const plainBuffer = await this.decryptFile(cipherBuffer, dek, ivBytes as BufferSource);
    const dec = new TextDecoder();
    return dec.decode(plainBuffer);
  }

  // Conversion Helpers
  private hexToUint8Array(hex: string): Uint8Array {
    const match = hex.match(/.{1,2}/g);
    return new Uint8Array(match ? match.map(byte => parseInt(byte, 16)) : []);
  }

  private arrayBufferToBase64(buffer: ArrayBuffer): string {
    let binary = '';
    const bytes = new Uint8Array(buffer);
    for (let i = 0; i < bytes.byteLength; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary);
  }

  private base64ToArrayBuffer(base64: string): ArrayBuffer {
    const binary = atob(base64);
    const len = binary.length;
    const bytes = new Uint8Array(len);
    for (let i = 0; i < len; i++) {
      bytes[i] = binary.charCodeAt(i);
    }
    return bytes.buffer as ArrayBuffer;

  }

  private uint8ArrayToBase64(arr: Uint8Array): string {
    return btoa(String.fromCharCode.apply(null, Array.from(arr)));
  }

  private base64ToUint8Array(base64: string): Uint8Array {
    return new Uint8Array(atob(base64).split("").map(c => c.charCodeAt(0)));
  }
}