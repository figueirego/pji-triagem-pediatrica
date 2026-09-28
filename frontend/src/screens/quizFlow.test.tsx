import { fireEvent, render } from '@testing-library/react-native';
import { QuizScreen } from './QuizScreen';
const questions = [{q:'Primeira?',type:'yesno' as const,weights:{yes:1,no:0,dunno:0}},{q:'Segunda?',type:'yesno' as const,weights:{yes:1,no:0,dunno:0}}];
it('requires explicit advancement and permits revising previous answers',async()=>{
 const finish=jest.fn();
 const view=await render(<QuizScreen onBack={jest.fn()} onFinish={finish} questions={questions} selectedChild={null} selectedSymptom={null}/>);
 await fireEvent.press(view.getByText('Sim'));
 await fireEvent.press(view.getByText('Sim'));
 expect(view.getByText('Primeira?')).toBeTruthy();
 await fireEvent.press(view.getByText('Próxima pergunta'));
 expect(view.getByText('Segunda?')).toBeTruthy();
 await fireEvent.press(view.getByLabelText('Voltar'));
 await fireEvent.press(view.getByText('Não'));
 await fireEvent.press(view.getByText('Próxima pergunta'));
 await fireEvent.press(view.getByText('Não'));
 await fireEvent.press(view.getByText('Concluir avaliação'));
 expect(finish).toHaveBeenCalledTimes(1);
 expect(finish).toHaveBeenCalledWith({0:'no',1:'no'});
});
