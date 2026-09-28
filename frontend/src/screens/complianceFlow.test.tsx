import {fireEvent,render} from '@testing-library/react-native';
import {AgeConfirmScreen} from './AgeConfirmScreen';
import {ResultScreen} from './ResultScreen';
import {HistoryScreen} from './HistoryScreen';
import type {ChildProfile} from '../types/domain';
const child:ChildProfile={id:'1',name:'Maria',age:'2 meses',initials:'M',weight:'Peso não informado',tint:'primary'};
it('requires explicit child age confirmation before progressing',async()=>{
 const next=jest.fn();
 const view=await render(<AgeConfirmScreen childrenList={[child]} selectedChild={child} onSelect={jest.fn()} onContinue={next} onAdd={jest.fn()} onEdit={jest.fn()} onBack={jest.fn()}/>);
 expect(next).not.toHaveBeenCalled();
 expect(view.getByText('Maria · 2 meses')).toBeTruthy();
 await fireEvent.press(view.getByText('Confirmar idade e continuar'));
 expect(next).toHaveBeenCalledTimes(1);
});
it('does not display low risk when no result exists',async()=>{
 const view=await render(<ResultScreen result={null} onBackHome={jest.fn()} onGoOrientations={jest.fn()}/>);
 expect(view.getByText('Resultado indisponível')).toBeTruthy();
 expect(view.queryByText('Baixo risco')).toBeNull();
});
it('keeps children with identical names separate by stable id',async()=>{
 const select=jest.fn();
 const view=await render(<HistoryScreen childrenList={[child,{...child,id:'2'}]} onBack={jest.fn()} onSelectAssessment={select} historyItems={[
 {id:'10',childId:'1',child:'Maria',symptom:'Febre',date:'Hoje',risk:'low'},
 {id:'20',childId:'2',child:'Maria',symptom:'Tosse',date:'Hoje',risk:'mod'},
 ]}/>);
 await fireEvent.press(view.getAllByRole('tab')[1]!);
 expect(view.getByText('Maria · Febre')).toBeTruthy();
 expect(view.queryByText('Maria · Tosse')).toBeNull();
 await fireEvent.press(view.getByText('Maria · Febre'));
 expect(select).toHaveBeenCalledWith(expect.objectContaining({id:'10'}));
});
it('renders classification reason only once',async()=>{
 const view=await render(<ResultScreen result={{risk:'high',score:8,hasRedFlag:true,reason:'Motivo único',protocolVersion:'v1'}} onBackHome={jest.fn()} onGoOrientations={jest.fn()}/>);
 expect(view.getAllByText('Motivo único')).toHaveLength(1);
});
