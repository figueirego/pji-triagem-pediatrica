import { api, unwrapData } from './api';
import { getToken, getUser } from './storage';
import { demoCredentials } from '../mocks/appMock';
import { mockLogin, mockMe, mockRegister } from '../mocks/authMock';
import type { AuthSession, AuthUser, LoginCredentials, RegisterPayload } from '../types/auth';

interface TokenResponse {
  accessToken: string;
  refreshToken?: string;
  accessTokenExpiration?: number;
  refreshTokenExpiration?: number;
}

interface AuthResponse {
  token: TokenResponse;
  usuario?: AuthUser;
}

interface TokenClaims {
  id?: number | string;
  login?: string;
  name?: string;
  sub?: string;
}

const useMockAuth = process.env.EXPO_PUBLIC_USE_MOCK_AUTH === 'true';
const base64Alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';

function normalizeDocument(value: string): string {
  return value.replace(/\D/g, '');
}

function bytesToUtf8(bytes: number[]): string {
  const encoded = bytes.map((byte) => `%${byte.toString(16).padStart(2, '0')}`).join('');
  try {
    return decodeURIComponent(encoded);
  } catch {
    return String.fromCharCode(...bytes);
  }
}

function decodeBase64(base64: string): string {
  const bytes: number[] = [];
  let buffer = 0;
  let bits = 0;

  for (const character of base64.replace(/=+$/, '')) {
    const value = base64Alphabet.indexOf(character);
    if (value < 0) continue;

    buffer = (buffer << 6) | value;
    bits += 6;
    if (bits >= 8) {
      bits -= 8;
      bytes.push((buffer >> bits) & 0xff);
    }
  }

  return bytesToUtf8(bytes);
}

function decodeBase64Url(value: string): string {
  const base64 = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');

  if (typeof globalThis.atob === 'function') {
    const binary = globalThis.atob(padded);
    return bytesToUtf8(Array.from(binary, (character) => character.charCodeAt(0)));
  }

  return decodeBase64(padded);
}

function decodeJwtClaims(token: string): TokenClaims {
  const [, payload] = token.split('.');
  if (!payload) return {};

  try {
    return JSON.parse(decodeBase64Url(payload)) as TokenClaims;
  } catch {
    return {};
  }
}

function buildUserFromToken(token: string, fallback: { email?: string; login: string; name?: string }): AuthUser {
  const claims = decodeJwtClaims(token);
  const login = claims.login || claims.sub || fallback.login;
  return {
    id: String(claims.id || login),
    login,
    name: claims.name || fallback.name || login,
    email: fallback.email || '',
  };
}

export async function loginRequest(credentials: LoginCredentials): Promise<AuthSession> {
  if (useMockAuth) {
    return mockLogin(credentials);
  }

  const login = normalizeDocument(credentials.login);
  const response = await api.post('/auth/login', { login, password: credentials.password });
  const tokenResponse = unwrapData<TokenResponse>(response.data);
  return {
    refreshToken: tokenResponse.refreshToken,
    token: tokenResponse.accessToken,
    user: buildUserFromToken(tokenResponse.accessToken, { login }),
  };
}

export async function registerRequest(payload: RegisterPayload): Promise<AuthSession> {
  if (useMockAuth) {
    return mockRegister(payload);
  }

  const login = normalizeDocument(payload.login);
  const response = await api.post('/auth/register/user', {
    email: payload.email.trim().toLowerCase(),
    login,
    name: payload.name.trim(),
    password: payload.password,
  });
  const authResponse = unwrapData<AuthResponse>(response.data);
  const session = {
    refreshToken: authResponse.token.refreshToken,
    token: authResponse.token.accessToken,
    user: buildUserFromToken(authResponse.token.accessToken, {
      email: payload.email.trim().toLowerCase(),
      login,
      name: payload.name.trim(),
    }),
  };
  return {
    ...session,
    user: {
      ...session.user,
      ...(authResponse.usuario || {}),
      email: payload.email.trim().toLowerCase(),
      name: payload.name.trim(),
    },
  };
}

export async function meRequest(): Promise<AuthUser> {
  const token = await getToken();
  const storedUser = await getUser();

  if (useMockAuth) {
    return mockMe(token, storedUser);
  }

  if (!token) {
    throw new Error('Sessão expirada.');
  }

  const response = await api.get('/auth/me');
  const currentUser = unwrapData<AuthUser>(response.data);
  return {
    ...currentUser,
    email: currentUser.email || storedUser?.email || '',
    id: String(currentUser.id),
    login: currentUser.login || storedUser?.login,
    name: currentUser.name || storedUser?.name || 'Cuidador',
  };
}

export function getDemoCredentials(): LoginCredentials {
  return demoCredentials;
}
