jest.mock('@react-native-async-storage/async-storage',()=>({getItem:jest.fn(),removeItem:jest.fn(),multiRemove:jest.fn(),setItem:jest.fn()}));
jest.mock('expo-secure-store',()=>({getItemAsync:jest.fn(),setItemAsync:jest.fn(),deleteItemAsync:jest.fn()}));
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as SecureStore from 'expo-secure-store';
import {getToken,setToken,clearAuth} from './storage';
it('migrates old plaintext token then deletes the old copy',async()=>{
 (SecureStore.getItemAsync as jest.Mock).mockResolvedValue(null);
 (AsyncStorage.getItem as jest.Mock).mockResolvedValue('legacy-token');
 expect(await getToken()).toBe('legacy-token');
 expect(SecureStore.setItemAsync).toHaveBeenCalledWith('peditriagem.token','legacy-token');
 expect(AsyncStorage.removeItem).toHaveBeenCalledWith('@peditriagem/token');
});
it('does not silently downgrade token persistence on secure storage failure',async()=>{
 (SecureStore.setItemAsync as jest.Mock).mockRejectedValueOnce(new Error('Keychain unavailable'));
 await expect(setToken('token')).rejects.toThrow('Keychain unavailable');
});
it('removes secure and legacy session data when signing out',async()=>{
 await clearAuth();
 expect(SecureStore.deleteItemAsync).toHaveBeenCalledWith('peditriagem.token');
 expect(AsyncStorage.multiRemove).toHaveBeenCalledWith(['@peditriagem/token','@peditriagem/user']);
});
