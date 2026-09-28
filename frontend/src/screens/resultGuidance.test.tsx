import { render } from '@testing-library/react-native';
import { ResultScreen } from './ResultScreen';
import { riskContent } from '../mocks/appMock';
it('uses matched assessment guidance instead of mocked clinical actions',async()=>{
 const risks={...riskContent,high:{...riskContent.high,actions:['Não oferecer comida ou líquido']}};
 const view=await render(<ResultScreen result={{assessmentId:1,risk:'high',score:8,hasRedFlag:true}} risks={risks} orientations={[{id:'main-1',title:'Avaliação imediata',subtitle:'Bebê com febre precisa de atendimento agora.',icon:'alert',tone:'high'}]} onBackHome={jest.fn()} onGoOrientations={jest.fn()}/>);
 expect(view.getByText('Avaliação imediata: Bebê com febre precisa de atendimento agora.')).toBeTruthy();
 expect(view.queryByText('Não oferecer comida ou líquido')).toBeNull();
});
it('keeps high urgency if high risk content is missing',async()=>{
 const view=await render(<ResultScreen result={{risk:'high',score:8,hasRedFlag:true}} risks={{low:riskContent.low} as typeof riskContent} onBackHome={jest.fn()} onGoOrientations={jest.fn()}/>);
 expect(view.getByText('Atendimento imediato')).toBeTruthy();
 expect(view.queryByText('Observação domiciliar')).toBeNull();
 expect(view.getByText(/Procure um serviço de emergência imediatamente/)).toBeTruthy();
});
it.each(['low','mod'] as const)('uses neutral fallback for %s without arbitrary schedules',async(risk)=>{
 const view=await render(<ResultScreen result={{risk,score:0,hasRedFlag:false}} risks={riskContent} onBackHome={jest.fn()} onGoOrientations={jest.fn()}/>);
 expect(view.queryByText('Reavaliar em 6h')).toBeNull();
 expect(view.queryByText('Agendar consulta')).toBeNull();
});
it('shows repeated backend title and description only once',async()=>{
 const view=await render(<ResultScreen result={{risk:'high',score:8,hasRedFlag:true}} orientations={[{id:'main-1',title:'Procure atendimento imediatamente',subtitle:'Procure atendimento imediatamente.',icon:'alert',tone:'high'}]} onBackHome={jest.fn()} onGoOrientations={jest.fn()}/>);
 expect(view.getByText('Procure atendimento imediatamente.')).toBeTruthy();
 expect(view.queryByText('Procure atendimento imediatamente: Procure atendimento imediatamente.')).toBeNull();
});
