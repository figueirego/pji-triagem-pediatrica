import {render} from '@testing-library/react-native';
import {SafeAreaInsetsContext} from 'react-native-safe-area-context';
import {AppViewport} from './AppViewport';
import {RegisterScreen} from '../screens/RegisterScreen';
jest.mock('../hooks/useAuth',()=>({useAuth:()=>({isSubmitting:false,register:jest.fn()})}));
it('keeps registration header and content inside notch and home indicator insets',async()=>{
 const view=await render(<SafeAreaInsetsContext.Provider value={{top:59,bottom:34,left:0,right:0}}><AppViewport><RegisterScreen onBack={jest.fn()}/></AppViewport></SafeAreaInsetsContext.Provider>);
 expect(view.getByTestId('app-safe-viewport')).toHaveStyle({paddingTop:59,paddingBottom:34,paddingLeft:0,paddingRight:0});
 expect(view.getByText('Criar conta')).toBeTruthy();
 expect(view.getByLabelText('Voltar')).toBeTruthy();
});
it('adapts to landscape safe insets without hardcoded status bar height',async()=>{
 const view=await render(<SafeAreaInsetsContext.Provider value={{top:0,bottom:21,left:59,right:59}}><AppViewport><RegisterScreen onBack={jest.fn()}/></AppViewport></SafeAreaInsetsContext.Provider>);
 expect(view.getByTestId('app-safe-viewport')).toHaveStyle({paddingTop:0,paddingBottom:21,paddingLeft:59,paddingRight:59});
});
