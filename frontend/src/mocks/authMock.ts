import { demoCredentials, demoUser } from './appMock';
import type { AuthSession, AuthUser, LoginCredentials, RegisterPayload } from '../types/auth';

export const MOCK_TOKEN = 'mock-token-peditriagem-demo';

type MockError = Error & { status?: number };

function wait(ms = 350): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export async function mockLogin({ login, password }: LoginCredentials): Promise<AuthSession> {
  await wait();

  const normalizedLogin = String(login || '').replace(/\D/g, '');
  if (!normalizedLogin || !password) {
    const error: MockError = new Error('Informe CPF e senha.');
    error.status = 400;
    throw error;
  }

  if (password.length < 4) {
    const error: MockError = new Error('Senha muito curta.');
    error.status = 401;
    throw error;
  }

  return {
    token: normalizedLogin === demoCredentials.login ? MOCK_TOKEN : `mock-token-${Date.now()}`,
    user: {
      ...demoUser,
      id: normalizedLogin,
      login: normalizedLogin,
      name: normalizedLogin === demoCredentials.login ? demoUser.name : 'Cuidador PediTriagem',
    },
  };
}

export async function mockRegister({ name, email, login, password }: RegisterPayload): Promise<AuthSession> {
  await wait();

  if (!name || !email || !login || !password) {
    const error: MockError = new Error('Preencha todos os campos obrigatórios.');
    error.status = 400;
    throw error;
  }

  return {
    token: `mock-token-${Date.now()}`,
    user: {
      email: String(email).trim().toLowerCase(),
      id: String(login).replace(/\D/g, ''),
      login: String(login).replace(/\D/g, ''),
      name: String(name).trim(),
    },
  };
}

export async function mockMe(token: string | null, storedUser: AuthUser | null): Promise<AuthUser> {
  await wait(180);

  if (!token || !String(token).startsWith('mock-token')) {
    const error: MockError = new Error('Sessão expirada.');
    error.status = 401;
    throw error;
  }

  return storedUser || demoUser;
}
