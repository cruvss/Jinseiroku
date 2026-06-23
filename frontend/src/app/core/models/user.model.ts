export interface User {
    id: string,
    email: string,
    encryptionSalt: string,
    timezone: string
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    email: string;
    recoveryKey: string;
    user: User;
}