import axios from 'axios';
import { clearAuth, getToken } from './storage';

export const API_BASE_URL = process.env.EXPO_PUBLIC_API_URL || 'http://localhost:8080';

export interface ApiEnvelope<T> {
  data: T;
  errors?: string[];
  links?: string[];
}

interface ApiErrorPayload {
  campos?: { campo?: string; erro?: string }[];
  errors?: string[];
  mensagem?: string;
  message?: string;
}

type UnauthorizedHandler = () => void | Promise<void>;

let unauthorizedHandler: UnauthorizedHandler | null = null;

export function setUnauthorizedHandler(handler: UnauthorizedHandler | null): void {
  unauthorizedHandler = handler;
}

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

api.interceptors.request.use(async (config) => {
  const token = await getToken();
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error?.response?.status === 401) {
      await clearAuth();
      if (unauthorizedHandler) await unauthorizedHandler();
    }

    return Promise.reject(error);
  },
);

export async function checkHealth(): Promise<unknown> {
  const response = await api.get<unknown>('/health');
  return response.data;
}

export function unwrapData<T>(payload: T | ApiEnvelope<T>): T {
  if (payload && typeof payload === 'object' && 'data' in payload) {
    return (payload as ApiEnvelope<T>).data;
  }

  return payload as T;
}

export function getApiErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError<ApiErrorPayload>(error)) {
    const payload = error.response?.data;
    const apiError = payload?.errors?.find(Boolean);
    const fieldError = payload?.campos?.[0];
    return apiError || fieldError?.erro || payload?.mensagem || payload?.message || error.message || fallback;
  }

  return error instanceof Error ? error.message : fallback;
}
