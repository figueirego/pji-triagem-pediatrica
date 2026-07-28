import { render } from '@testing-library/react-native';
import { ChildFormScreen } from './ChildFormScreen';
import { HistoryScreen } from './HistoryScreen';
import { LoginScreen } from './LoginScreen';
import { ProfileScreen } from './ProfileScreen';
import { QuizScreen } from './QuizScreen';
import { ResultScreen } from './ResultScreen';
import { SymptomsScreen } from './SymptomsScreen';
import { formatAge, parseAgeAmount } from '../utils/age';
import type { ChildProfile, RiskContentMap, Symptom, TriageQuestion, TriageResult } from '../types/domain';

jest.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    authError: null,
    isSubmitting: false,
    login: jest.fn(),
    logout: jest.fn(),
    register: jest.fn(),
    user: { id: '1', login: '52998224725', name: 'Camila Ribeiro', email: 'camila@example.test' },
  }),
}));

const child: ChildProfile = {
  age: '4 anos',
  ageUnit: 'anos',
  ageValue: '4',
  avatarEmoji: '🌸',
  id: '1',
  initials: '🌸',
  name: 'Maria',
  tint: 'primary',
  weight: '18 kg',
};

const symptom: Symptom = {
  code: 'FEBRE',
  desc: 'Temperatura acima de 37.8°C',
  icon: 'thermo',
  id: '1',
  name: 'Febre',
  tone: 'mod',
};

const question: TriageQuestion = {
  q: 'A criança está prostrada?',
  sub: 'Observe o estado geral.',
  type: 'yesno',
  weights: { dunno: 0, no: 0, yes: 6 },
};

const risks: RiskContentMap = {
  high: {
    actions: ['Procure atendimento imediatamente'],
    cta: 'Ligar para SAMU',
    icon: 'phone',
    message: 'Sinais importantes pedem avaliação urgente.',
    mood: 'alert',
    title: 'Alto risco',
  },
  low: {
    actions: ['Observe a evolução'],
    cta: 'Ver orientações',
    icon: 'info',
    message: 'Sinais leves no momento.',
    mood: 'calm',
    title: 'Baixo risco',
  },
  mod: {
    actions: ['Acompanhe de perto'],
    cta: 'Ver orientações',
    icon: 'warn',
    message: 'Sinais moderados pedem atenção.',
    mood: 'watch',
    title: 'Risco moderado',
  },
};

const result: TriageResult = {
  answeredAt: '2026-07-06T12:00:00.000Z',
  child,
  hasRedFlag: false,
  risk: 'mod',
  score: 5,
  symptom,
};

describe('main screen smoke tests', () => {
  it('renders Login screen', async () => {
    const view = await render(<LoginScreen onNavigate={jest.fn()} />);
    expect(view.getByText('PediTriagem')).toBeTruthy();
  });

  it('renders ChildForm screen', async () => {
    const view = await render(<ChildFormScreen onBack={jest.fn()} onSave={jest.fn()} />);
    expect(view.getByText('Cadastrar criança')).toBeTruthy();
  });

  it('renders Symptoms screen', async () => {
    const view = await render(<SymptomsScreen onBack={jest.fn()} onSelectSymptom={jest.fn()} selectedChild={child} symptoms={[symptom]} />);
    expect(view.getByText('Febre')).toBeTruthy();
  });

  it('renders Quiz screen', async () => {
    const view = await render(<QuizScreen onBack={jest.fn()} onFinish={jest.fn()} questions={[question]} selectedChild={child} selectedSymptom={symptom} />);
    expect(view.getByText('A criança está prostrada?')).toBeTruthy();
  });

  it('renders Quiz empty state when runtime data sends null questions', async () => {
    const view = await render(
      <QuizScreen
        onBack={jest.fn()}
        onFinish={jest.fn()}
        questions={null as unknown as TriageQuestion[]}
        selectedChild={child}
        selectedSymptom={symptom}
      />,
    );

    expect(view.getByText('Questionário indisponível')).toBeTruthy();
  });

  it('renders Result screen', async () => {
    const view = await render(<ResultScreen onBackHome={jest.fn()} onGoOrientations={jest.fn()} result={result} risks={risks} />);
    expect(view.getAllByText('Risco moderado').length).toBeGreaterThan(0);
  });

  it('renders History screen', async () => {
    const view = await render(<HistoryScreen childrenList={[child]} historyItems={[]} onBack={jest.fn()} />);
    expect(view.getByText('Nenhuma avaliação ainda')).toBeTruthy();
  });

  it('renders Profile screen', async () => {
    const view = await render(<ProfileScreen childrenList={[child]} onEditChild={jest.fn()} onGo={jest.fn()} />);
    expect(view.getByText('Camila Ribeiro')).toBeTruthy();
  });
});

describe('formatAge', () => {
  it('formats months and years', () => {
    expect(formatAge(8)).toBe('8 meses');
    expect(formatAge(48)).toBe('4 anos');
    expect(formatAge(14)).toBe('1 ano e 2 meses');
  });

  it('rejects invalid pediatric age values', () => {
    expect(() => parseAgeAmount('abc', 'anos')).toThrow('Informe uma idade válida.');
    expect(() => parseAgeAmount('13', 'anos')).toThrow('A criança deve ter até 12 anos.');
  });
});
