import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { STORAGE_KEYS } from './storageKeys';
import type { AuthUser } from '../types/auth';

async function getString(key: string): Promise<string | null> {
  try {
    return await AsyncStorage.getItem(key);
  } catch {
    return null;
  }
}

async function getJson<T>(key: string): Promise<T | null> {
  const value = await getString(key);
  if (!value) return null;

  try {
    return JSON.parse(value) as T;
  } catch {
    return null;
  }
}

async function setJson<T>(key: string, value: T): Promise<void> {
  try {
    await AsyncStorage.setItem(key, JSON.stringify(value));
  } catch {
    // Storage failures should not crash UI flows in Expo Go demos.
  }
}

const secureTokenKey = 'peditriagem.token';

export async function getToken(): Promise<string | null> {
  const token = Platform.OS === 'web'
    ? globalThis.sessionStorage?.getItem(secureTokenKey) ?? null
    : await SecureStore.getItemAsync(secureTokenKey);
  if (token) {
    await AsyncStorage.removeItem(STORAGE_KEYS.token);
    return token;
  }
  const legacyToken = await AsyncStorage.getItem(STORAGE_KEYS.token);
  if (legacyToken) await setToken(legacyToken);
  return legacyToken;
}

export async function setToken(token: string | null | undefined): Promise<void> {
  if (Platform.OS === 'web') {
    if (token) globalThis.sessionStorage.setItem(secureTokenKey, token);
    else globalThis.sessionStorage.removeItem(secureTokenKey);
  } else if (token) {
    await SecureStore.setItemAsync(secureTokenKey, token);
  } else {
    await SecureStore.deleteItemAsync(secureTokenKey);
  }
  await AsyncStorage.removeItem(STORAGE_KEYS.token);
}

export async function getUser(): Promise<AuthUser | null> {
  return getJson<AuthUser>(STORAGE_KEYS.user);
}

export async function setUser(user: AuthUser | null | undefined): Promise<void> {
  if (!user) {
    try {
      await AsyncStorage.removeItem(STORAGE_KEYS.user);
    } catch {
      // no-op
    }
    return;
  }

  await setJson(STORAGE_KEYS.user, user);
}

export async function clearAuth(): Promise<void> {
  await setToken(null);
  await AsyncStorage.multiRemove([STORAGE_KEYS.token, STORAGE_KEYS.user]);
}

export async function getDisclaimerAcepto(): Promise<boolean | null> {
  const value = await getString(STORAGE_KEYS.disclaimerAccepted);
  if (value === null) return null;
  return value === 'true';
}

export async function setDisclaimerAcepto(accepted: boolean): Promise<void> {
  try {
    await AsyncStorage.setItem(STORAGE_KEYS.disclaimerAccepted, accepted ? 'true' : 'false');
  } catch {
    // no-op
  }
}

export const getDisclaimerAccepted = getDisclaimerAcepto;
export const setDisclaimerAccepted = setDisclaimerAcepto;
