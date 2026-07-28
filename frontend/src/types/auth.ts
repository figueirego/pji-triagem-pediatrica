export interface AuthUser {
  id: string;
  name: string;
  email: string;
  login?: string;
}

export interface AuthSession {
  token: string;
  refreshToken?: string;
  user: AuthUser;
}

export interface LoginCredentials {
  login: string;
  password: string;
}

export interface RegisterPayload {
  login: string;
  name: string;
  email: string;
  password: string;
}
