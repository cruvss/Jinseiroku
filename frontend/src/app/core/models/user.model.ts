export interface User {
    id: string,
    email: string,
    encryptionSalt: string,
    timezone: string
}

export interface AuthResponse {
    accessToken: string;
    user: User;
}