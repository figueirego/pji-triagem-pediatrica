import axios from 'axios';
import { createContext, useCallback, useEffect, useMemo, useState } from 'react';
import { setUnauthorizedHandler } from '../services/api';
import { loginRequest, meRequest, registerRequest } from '../services/authService';
import { clearAuth, getToken, setToken, setUser } from '../services/storage';
import type { ReactNode } from 'react';
import type { AuthSession, AuthUser, LoginCredentials, RegisterPayload } from '../types/auth';

interface AuthProviderProps {
  children: ReactNode;
}

export interface AuthContextValue {
  authError: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  isSubmitting: boolean;
  login: (credentials: LoginCredentials) => Promise<AuthSession>;
  logout: () => Promise<void>;
  register: (payload: RegisterPayload) => Promise<AuthSession>;
  token: string | null;
  user: AuthUser | null;
}

interface ErrorResponseData {
  campos?: { campo?: string; erro?: string }[];
  errors?: string[];
  mensagem?: string;
  message?: string;
}

export const AuthContext = createContext<AuthContextValue | null>(null);

function getAuthErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError<ErrorResponseData>(error)) {
    const payload = error.response?.data;
    return payload?.errors?.find(Boolean) || payload?.campos?.[0]?.erro || payload?.mensagem || payload?.message || error.message || fallback;
  }

  return error instanceof Error ? error.message : fallback;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUserState] = useState<AuthUser | null>(null);
  const [token, setTokenState] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [authError, setAuthError] = useState<string | null>(null);

  const resetSession = useCallback(async () => {
    await clearAuth();
    setUserState(null);
    setTokenState(null);
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      resetSession();
    });

    return () => setUnauthorizedHandler(null);
  }, [resetSession]);

  useEffect(() => {
    let mounted = true;

    async function hydrateSession() {
      setIsLoading(true);
      const storedToken = await getToken();

      if (!storedToken) {
        if (mounted) setIsLoading(false);
        return;
      }

      try {
        const currentUser = await meRequest();
        if (!mounted) return;
        setTokenState(storedToken);
        setUserState(currentUser);
      } catch {
        await clearAuth();
        if (!mounted) return;
        setTokenState(null);
        setUserState(null);
      } finally {
        if (mounted) setIsLoading(false);
      }
    }

    hydrateSession();

    return () => {
      mounted = false;
    };
  }, []);

  const persistSession = useCallback(async (session: AuthSession) => {
    await setToken(session.token);
    await setUser(session.user);
    setTokenState(session.token);
    setUserState(session.user);
  }, []);

  const login = useCallback(
    async (credentials: LoginCredentials) => {
      setIsSubmitting(true);
      setAuthError(null);
      try {
        const session = await loginRequest(credentials);
        await persistSession(session);
        return session;
      } catch (error: unknown) {
        const message = getAuthErrorMessage(error, 'Não foi possível entrar.');
        setAuthError(message);
        throw new Error(message);
      } finally {
        setIsSubmitting(false);
      }
    },
    [persistSession],
  );

  const register = useCallback(
    async (payload: RegisterPayload) => {
      setIsSubmitting(true);
      setAuthError(null);
      try {
        const session = await registerRequest(payload);
        await persistSession(session);
        return session;
      } catch (error: unknown) {
        const message = getAuthErrorMessage(error, 'Não foi possível criar a conta.');
        setAuthError(message);
        throw new Error(message);
      } finally {
        setIsSubmitting(false);
      }
    },
    [persistSession],
  );

  const logout = useCallback(async () => {
    setAuthError(null);
    await resetSession();
  }, [resetSession]);

  const value = useMemo<AuthContextValue>(
    () => ({
      authError,
      isAuthenticated: Boolean(user && token),
      isLoading,
      isSubmitting,
      login,
      logout,
      register,
      token,
      user,
    }),
    [authError, isLoading, isSubmitting, login, logout, register, token, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
