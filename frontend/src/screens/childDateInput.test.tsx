import {fireEvent,render} from '@testing-library/react-native';
import {ChildFormScreen} from './ChildFormScreen';
import type {ChildProfile} from '../types/domain';
const child:ChildProfile={id:'1',name:'Maria',birthDate:'2024-02-29',age:'2 anos e 6 meses',ageValue:'30',ageUnit:'meses',weight:'12 kg',initials:'M',tint:'primary'};
it('loads localized date and preserves precise ISO birthdate on edit',async()=>{
 const save=jest.fn();
 const view=await render(<ChildFormScreen initialChild={child} onBack={jest.fn()} onSave={save}/>);
 expect(view.getByDisplayValue('29/02/2024')).toBeTruthy();
 await fireEvent.press(view.getByText('Salvar alterações'));
 expect(save).toHaveBeenCalledWith(expect.objectContaining({birthDate:'2024-02-29',weight:'12 kg'}));
});
it('masks digit paste, refuses impossible dates, and saves valid formatted dates as ISO',async()=>{
 const save=jest.fn();
 const view=await render(<ChildFormScreen initialChild={child} onBack={jest.fn()} onSave={save}/>);
 await fireEvent.changeText(view.getByLabelText('Data de nascimento'),'31022024');
 expect(view.getByDisplayValue('31/02/2024')).toBeTruthy();
 await fireEvent.press(view.getByText('Salvar alterações'));
 expect(save).not.toHaveBeenCalled();
 expect(view.getByText('Data de nascimento inválida.')).toBeTruthy();
 await fireEvent.changeText(view.getByLabelText('Data de nascimento'),'01092024');
 await fireEvent.press(view.getByText('Salvar alterações'));
 expect(save).toHaveBeenCalledWith(expect.objectContaining({birthDate:'2024-09-01'}));
});
it('keeps age-only entry available when birthdate is empty',async()=>{
 const save=jest.fn();
 const view=await render(<ChildFormScreen initialChild={{...child,birthDate:undefined}} onBack={jest.fn()} onSave={save}/>);
 await fireEvent.press(view.getByText('Salvar alterações'));
 expect(save).toHaveBeenCalledWith(expect.objectContaining({birthDate:undefined,ageValue:'30',ageUnit:'meses'}));
});
