import type {
  ChildProfile,
  HistoryItem,
  OrientationCardItem,
  RiskContentMap,
  Symptom,
  TriageQuestion,
} from '../types/domain';
import type { AuthUser, LoginCredentials } from '../types/auth';

export const demoUser: AuthUser = {
  id: 'user-demo',
  login: '52998224725',
  name: 'Camila Ribeiro',
  email: 'camila.demo@peditriagem.app',
};

export const demoCredentials: LoginCredentials = {
  login: demoUser.login || '52998224725',
  password: 'peditriagem123',
};

export const initialChildren: ChildProfile[] = [
  {
    id: 'maria',
    name: 'Maria',
    ageValue: '4',
    ageUnit: 'anos',
    age: '4 anos',
    weight: '17 kg',
    initials: 'MA',
    tint: 'low',
  },
  {
    id: 'lucas',
    name: 'Lucas',
    ageValue: '8',
    ageUnit: 'meses',
    age: '8 meses',
    weight: '8.2 kg',
    initials: 'LU',
    tint: 'primary',
  },
];

export const symptoms: Symptom[] = [
  {
    id: 'fever',
    name: 'Febre',
    desc: 'Temperatura acima de 37,8°C',
    icon: 'thermo',
    tone: 'mod',
  },
  {
    id: 'cough',
    name: 'Tosse',
    desc: 'Seca, com catarro ou persistente',
    icon: 'cough',
    tone: 'primary',
  },
  {
    id: 'vomit',
    name: 'Vômitos',
    desc: 'Náuseas ou episódios de vômito',
    icon: 'vomit',
    tone: 'neutral',
  },
  {
    id: 'diarrhea',
    name: 'Diarreia',
    desc: 'Fezes líquidas ou frequentes',
    icon: 'drop',
    tone: 'primary',
  },
  {
    id: 'belly',
    name: 'Dor abdominal',
    desc: 'Dor ou desconforto na barriga',
    icon: 'activity',
    tone: 'mod',
  },
  {
    id: 'breath',
    name: 'Falta de ar',
    desc: 'Respiração rápida ou difícil',
    icon: 'activity',
    tone: 'high',
  },
  {
    id: 'rash',
    name: 'Manchas na pele',
    desc: 'Vermelhidão, pintas ou erupção',
    icon: 'rash',
    tone: 'high',
  },
  {
    id: 'trauma',
    name: 'Trauma leve',
    desc: 'Quedas, batidas ou cortes pequenos',
    icon: 'trauma',
    tone: 'low',
  },
  {
    id: 'ear',
    name: 'Dor de ouvido',
    desc: 'Dor, coceira ou secreção',
    icon: 'info',
    tone: 'primary',
  },
];

export const feverQuiz: TriageQuestion[] = [
  {
    q: 'Qual a temperatura medida agora?',
    sub: 'Termômetro axilar é o mais comum.',
    type: 'options',
    options: [
      { id: 't1', label: 'Abaixo de 37,8°C', risk: 0 },
      { id: 't2', label: 'Entre 37,8 e 38,5°C', risk: 1 },
      { id: 't3', label: 'Entre 38,5 e 39,5°C', risk: 2 },
      { id: 't4', label: 'Acima de 39,5°C', risk: 3 },
    ],
  },
  {
    q: 'Há quanto tempo a febre começou?',
    type: 'options',
    options: [
      { id: 'd1', label: 'Menos de 24 horas', risk: 0 },
      { id: 'd2', label: '1 a 3 dias', risk: 1 },
      { id: 'd3', label: 'Mais de 3 dias', risk: 2 },
    ],
  },
  {
    q: 'A criança está prostrada ou muito sonolenta?',
    sub: 'Difícil de acordar, sem energia para brincar.',
    type: 'yesno',
    weights: { yes: 3, no: 0, dunno: 1 },
  },
  {
    q: 'Há dificuldade para respirar?',
    sub: 'Respiração rápida, ofegante ou ruidosa.',
    type: 'yesno',
    weights: { yes: 3, no: 0, dunno: 2 },
  },
  {
    q: 'A criança está bebendo líquidos normalmente?',
    type: 'yesno',
    weights: { yes: 0, no: 2, dunno: 1 },
  },
  {
    q: 'Apareceram manchas na pele que não somem ao apertar?',
    sub: 'Pequenas pintinhas vermelhas ou roxas.',
    type: 'yesno',
    weights: { yes: 3, no: 0, dunno: 1 },
    redFlag: true,
  },
];

export const riskContent: RiskContentMap = {
  low: {
    title: 'Observar em casa',
    message: 'No momento, os sinais informados parecem leves. Acompanhe a evolução e siga as orientações.',
    cta: 'Ver cuidados em casa',
    icon: 'check',
    mood: 'calm',
    actions: ['Hidratação', 'Repouso', 'Reavaliar em 6h'],
  },
  mod: {
    title: 'Procure avaliação médica em até 24h',
    message: 'A criança apresenta sinais que precisam de avaliação profissional nas próximas horas.',
    cta: 'Ver cuidados até a consulta',
    icon: 'warn',
    mood: 'watch',
    actions: ['Agendar consulta', 'Monitorar febre', 'Hidratação reforçada'],
  },
  high: {
    title: 'Procure emergência imediatamente',
    message: 'Os sinais informados indicam necessidade de atendimento médico urgente.',
    cta: 'Ver orientações imediatas',
    icon: 'alert',
    mood: 'alert',
    actions: ['Não oferecer comida ou líquido', 'Manter a criança confortável', 'Levar à UPA mais próxima'],
  },
};

export const historyItems: HistoryItem[] = [
  { id: 'h1', child: 'Maria', symptom: 'Febre', date: '12 mai 2026', risk: 'low' },
  { id: 'h2', child: 'Maria', symptom: 'Tosse', date: '04 mai 2026', risk: 'mod' },
  { id: 'h3', child: 'Lucas', symptom: 'Vômitos', date: '28 abr 2026', risk: 'low' },
  { id: 'h4', child: 'Maria', symptom: 'Trauma leve', date: '15 abr 2026', risk: 'low' },
  { id: 'h5', child: 'Lucas', symptom: 'Falta de ar', date: '02 abr 2026', risk: 'high' },
];

export const orientationCards: OrientationCardItem[] = [
  {
    id: 'observe',
    title: 'O que observar',
    subtitle: 'Sinais para acompanhar ao longo do dia',
    icon: 'heart',
    tone: 'primary',
  },
  {
    id: 'worry',
    title: 'Quando se preocupar',
    subtitle: 'Sintomas que pedem atenção rápida',
    icon: 'warn',
    tone: 'mod',
  },
  {
    id: 'home-care',
    title: 'Cuidados em casa',
    subtitle: 'Hidratação, repouso e medicação orientada',
    icon: 'pill',
    tone: 'low',
  },
  {
    id: 'help',
    title: 'Quando procurar ajuda',
    subtitle: 'Sinais para ir à emergência sem demora',
    icon: 'phone',
    tone: 'high',
  },
];
