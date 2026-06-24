export interface User {
    id?: string;
    email: string;
    encryptionSalt?: string;
    timezone?: string;
}

export interface AuthResponse {
    accessToken?: string;
    refreshToken?: string;
    email: string;
    recoveryKey?: string;
    encryptionSalt: string;
    encryptedKekVerification?: string;
}

export interface VaultParamsResponse {
    encryptionSalt: string;
    encryptedKekVerification?: string;
}

export interface ApiResponse<T> {
    timestamp: string;
    statusCode: number;
    success: boolean;
    message: string;
    data: T;

}