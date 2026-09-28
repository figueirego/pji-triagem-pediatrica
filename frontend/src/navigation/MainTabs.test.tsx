import {Alert} from 'react-native';
import {fireEvent,render,waitFor} from '@testing-library/react-native';
import {MainTabs} from './MainTabs';
import {getAssessmentResult,getOrientationCards} from '../services/pediatricService';
const mockData = {
 children:[{id:'1',name:'Maria',age:'2 meses',initials:'M',weight:'',tint:'primary'}],
 history:[{id:'10',childId:'1',child:'Maria',symptom:'Febre',date:'Hoje',risk:'high'}],
 orientations:[{id:'old',title:'Orientação antiga',subtitle:'Outra avaliação',icon:'book',tone:'low'}],
 symptoms:[],risks:{high:{title:'Atendimento imediato',message:'Procure emergência',actions:[],icon:'alert',mood:'alert',cta:'Ver orientações imediatas'}},
};
jest.mock('../hooks/useAuth',()=>({useAuth:()=>({user:{id:'1',name:'Cuidador'}})}));
jest.mock('../hooks/usePediatricData',()=>({usePediatricData:()=>({data:mockData,error:null,isLoading:false,reload:jest.fn()})}));
jest.mock('../services/pediatricService',()=>({getAssessmentResult:jest.fn(),getOrientationCards:jest.fn()}));
it('opens saved result when guidance fails and clears unrelated guidance',async()=>{
 jest.spyOn(Alert,'alert').mockImplementation(()=>{});
 (getAssessmentResult as jest.Mock).mockResolvedValue({assessmentId:10,risk:'high',score:8,hasRedFlag:true,reason:'Febre em bebê',child:mockData.children[0]});
 (getOrientationCards as jest.Mock).mockRejectedValue(new Error('offline'));
 const view=await render(<MainTabs/>);
 await fireEvent.press(view.getByText('Maria · Febre'));
 await waitFor(()=>expect(view.getByText('Atendimento imediato')).toBeTruthy());
 await fireEvent.press(view.getByText('Ver orientações imediatas'));
 expect(view.queryByText('Orientação antiga')).toBeNull();
});
