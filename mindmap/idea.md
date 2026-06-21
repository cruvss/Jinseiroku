# 🛡️ OmniSafe: Project Specification & Concept

## Executive Summary
OmniSafe is a privacy-first, zero-knowledge personal data dashboard. In an era where digital identities, communications, and personal records are scattered across dozens of vulnerable platforms, OmniSafe provides a single, unified interface to manage a user's entire digital life securely. 

The core philosophy of OmniSafe is **absolute data sovereignty**. It is designed to aggregate calendars, emails, and sensitive life documents while enforcing stringent access controls, ensuring that the user is the sole custodian of their data.

## Core Modules & Functionality

### 1. 🔐 The Zero-Knowledge Vault
A secure document management system categorized by life domains (Identity, Education, Health, Legal). 
* **Concept:** Files are encrypted client-side before transmission. The server acts merely as a blind storage unit for encrypted blobs.
* **Security Posture:** Even in the event of a database breach or a direct request to the server host, the documents remain inaccessible without the user's local decryption key.

### 2. ⏳ Life Timeline
A chronological logging system for major life milestones (career changes, education, personal events). 
* **Concept:** Moves beyond standard resumes or fragmented social media profiles to provide a private, holistic view of personal progression.

### 3. 📥 Unified Inbox & 📅 Calendar
An integration hub that syncs with external providers (e.g., Google Workspace, Outlook).
* **Concept:** Aggregates communications and schedules into a single view. Uses secure OAuth 2.0 flows to fetch data without storing third-party plaintext passwords.

### 4. ⚙️ System Security & Identity
The command center for the user's digital perimeter.
* **Concept:** Manages active sessions, connected OAuth integrations, and multi-factor authentication settings.

## System Architecture & Security Requirements

To move this project from a frontend UI to a production-ready application, a robust backend infrastructure is strictly required. The architecture must prioritize data integrity and access control.

### Authentication & Authorization
The system relies on a stateless, token-based architecture:
* **JWT (JSON Web Tokens):** The primary mechanism for managing secure sessions. Upon passing primary credentials and the 2FA challenge, the user is issued a short-lived access token and a secure, HttpOnly refresh token.
* **Granular Authorization:** Every API request to the vault or timeline must validate the JWT signature and ensure the token's subject (`userId`) explicitly matches the resource owner. 
* **Multi-Factor Authentication (MFA):** Enforced via time-based one-time passwords (TOTP) to protect against credential stuffing.

### Data Governance & Risk Management
* **Threat Mitigation:** By utilizing end-to-end encryption for the vault and keeping OAuth tokens securely vaulted on the backend, the attack surface is significantly minimized.
* **Compliance Ready:** Designed with a structural foundation that supports strict data privacy frameworks, ensuring clear policies around data retention, user deletion rights, and secure unmapping of third-party integrations.

## Target Audience
OmniSafe is built for privacy-conscious individuals, professionals managing sensitive contracts, and anyone seeking to consolidate their digital footprint into a secure, verifiable, and private environment.
