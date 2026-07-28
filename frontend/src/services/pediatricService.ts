import { api, unwrapData } from './api';
import { orientationCards, riskContent } from '../mocks/appMock';
import { parseAge, parseAgeAmount } from '../utils/age';
import type {
  AgeUnit,
  ChildProfile,
  HistoryItem,
  IconName,
  OrientationCardItem,
  PediatricDemoData,
  RiskContentMap,
  RiskLevel,
  RiskTone,
  Symptom,
  TriageAnswers,
  TriageQuestion,
  TriageResult,
  YesNoAnswer,
} from '../types/domain';

type BackendQuestionType = 'YESNO' | 'OPTIONS';
type BackendClassification = 'LOW' | 'MOD' | 'HIGH';

interface ChildHomeResponse {
  id: number;
  name: string;
  age?: string;
  ageInMonths?: number;
  avatarEmoji?: string;
}

interface ChildResponse extends ChildHomeResponse {
  cpf?: string;
  birthDate?: string;
  weightKg?: number | string;
}

interface SymptomResponse {
  id: number;
  code: string;
  name: string;
  shortDescription?: string;
  iconRef?: string;
  colorHex?: string;
  order?: number;
}

interface QuestionOptionResponse {
  id: number;
  code: string;
  text: string;
  order?: number;
}

interface QuestionResponse {
  id: number;
  code: string;
  text: string;
  subtitle?: string;
  type: BackendQuestionType;
  order?: number;
  options?: QuestionOptionResponse[];
}

interface QuestionnaireResponse {
  symptoms?: {
    symptomId: number;
    code: string;
    name: string;
    questions?: QuestionResponse[];
  }[];
}

interface AssessmentResultResponse {
  assessmentId: number;
  childId: number;
  finalClassification: BackendClassification;
  totalScore?: number;
  redFlagDetected?: boolean;
}

interface OrientationItemResponse {
  id?: number;
  title: string;
  description?: string;
  symptomName?: string;
}

interface OrientationResponse {
  mainActions?: OrientationItemResponse[];
  warningSigns?: OrientationItemResponse[];
  homeCare?: OrientationItemResponse[];
  whenSeekHelp?: OrientationItemResponse[];
}

interface AssessmentHistoryItemResponse {
  id: number;
  childId?: number;
  childName: string;
  symptoms?: string[];
  createdAt?: string;
  classification: BackendClassification;
}

interface AssessmentHistoryResponse {
  assessments?: AssessmentHistoryItemResponse[];
}

function toNumericId(value: string | number | undefined, label: string): number {
  const id = typeof value === 'number' ? value : Number(value);
  if (!Number.isFinite(id) || id <= 0) {
    throw new Error(`${label} inválido para sincronizar com o backend.`);
  }
  return id;
}

function initials(name: string): string {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0))
    .join('')
    .toUpperCase();
}

function mapClassification(classification?: BackendClassification): RiskLevel {
  if (classification === 'HIGH') return 'high';
  if (classification === 'MOD') return 'mod';
  return 'low';
}

function mapSymptomTone(symptom: SymptomResponse): RiskTone {
  if (['FALTA_DE_AR', 'MANCHAS_PELE'].includes(symptom.code)) return 'high';
  if (['FEBRE', 'DOR_ABDOMINAL'].includes(symptom.code)) return 'mod';
  if (symptom.code === 'TRAUMA_LEVE') return 'low';
  return 'primary';
}

function mapIcon(name?: string): IconName {
  const normalized = String(name || '').trim();
  const allowed: IconName[] = [
    'activity',
    'alert',
    'baby',
    'bandage',
    'belly',
    'book',
    'cough',
    'drop',
    'ear',
    'heart',
    'info',
    'lung',
    'pill',
    'rash',
    'thermo',
    'trauma',
    'vomit',
    'warn',
  ];
  return allowed.includes(normalized as IconName) ? (normalized as IconName) : 'activity';
}

function mapChild(child: ChildHomeResponse | ChildResponse): ChildProfile {
  const age = parseAge(child.age, child.ageInMonths);
  const weight = 'weightKg' in child && child.weightKg ? `${String(child.weightKg).replace('.', ',')} kg` : 'Peso não informado';
  return {
    ...age,
    avatarEmoji: child.avatarEmoji,
    backendId: child.id,
    id: String(child.id),
    initials: child.avatarEmoji || initials(child.name),
    name: child.name,
    tint: 'primary',
    weight,
  };
}

function mapSymptom(symptom: SymptomResponse): Symptom {
  return {
    backendId: symptom.id,
    code: symptom.code,
    colorHex: symptom.colorHex,
    desc: symptom.shortDescription || 'Questionário pediátrico disponível',
    icon: mapIcon(symptom.iconRef),
    id: String(symptom.id),
    name: symptom.name,
    tone: mapSymptomTone(symptom),
  };
}

function mapQuestion(question: QuestionResponse): TriageQuestion {
  if (question.type === 'OPTIONS') {
    return {
      backendId: question.id,
      options: [...(question.options || [])]
        .sort((a, b) => (a.order || 0) - (b.order || 0))
        .map((option) => ({
          backendId: option.id,
          id: String(option.id),
          label: option.text,
        })),
      q: question.text,
      sub: question.subtitle,
      type: 'options',
    };
  }

  return {
    backendId: question.id,
    q: question.text,
    sub: question.subtitle,
    type: 'yesno',
    weights: { dunno: 0, no: 0, yes: 0 },
  };
}

function ageToBirthDate(ageValue?: string, ageUnit?: AgeUnit): string {
  const amount = parseAgeAmount(ageValue, ageUnit || 'anos');
  const date = new Date();
  if (ageUnit === 'meses') {
    date.setMonth(date.getMonth() - amount);
  } else {
    date.setFullYear(date.getFullYear() - amount);
  }
  return date.toISOString().slice(0, 10);
}

function parseWeightKg(weight: string): number | undefined {
  const match = weight.replace(',', '.').match(/\d+(\.\d+)?/);
  if (!match?.[0]) return undefined;
  const parsed = Number(match[0]);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined;
}

function toChildRequest(child: ChildProfile) {
  return {
    avatarEmoji: child.avatarEmoji || '🌸',
    birthDate: ageToBirthDate(child.ageValue, child.ageUnit),
    cpf: '',
    name: child.name,
    weightKg: parseWeightKg(child.weight),
  };
}

function mapAnswer(value: string): YesNoAnswer {
  if (value === 'yes' || value === 'no' || value === 'dunno') return value;
  return 'dunno';
}

function formatHistoryDate(value?: string): string {
  if (!value) return 'Agora';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'Agora';

  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(date);
}

function mapHistoryItem(item: AssessmentHistoryItemResponse): HistoryItem {
  return {
    child: item.childName,
    date: formatHistoryDate(item.createdAt),
    id: String(item.id),
    risk: mapClassification(item.classification),
    symptom: item.symptoms?.join(', ') || 'Sintomas avaliados',
  };
}

export async function getChildren(userId?: string | number | null): Promise<ChildProfile[]> {
  if (!userId) return [];

  const response = await api.get(`/children/user/${toNumericId(userId, 'Usuário')}`);
  const children = unwrapData<ChildHomeResponse[]>(response.data);
  return children.map(mapChild);
}

export async function createChild(userId: string | number, child: ChildProfile): Promise<ChildProfile> {
  const response = await api.post(`/children/${toNumericId(userId, 'Usuário')}`, toChildRequest(child));

  return mapChild(unwrapData<ChildResponse>(response.data));
}

export async function updateChild(userId: string | number, child: ChildProfile): Promise<ChildProfile> {
  const response = await api.put(
    `/children/${toNumericId(userId, 'Usuário')}/children/${toNumericId(child.backendId || child.id, 'Criança')}`,
    toChildRequest(child),
  );

  return mapChild(unwrapData<ChildResponse>(response.data));
}

export async function deleteChild(userId: string | number, childId: string | number): Promise<void> {
  await api.delete(`/children/${toNumericId(userId, 'Usuário')}/children/${toNumericId(childId, 'Criança')}`);
}

export async function getSymptoms(): Promise<Symptom[]> {
  const response = await api.get('/symptoms');
  const symptoms = unwrapData<SymptomResponse[]>(response.data);
  return [...symptoms].sort((a, b) => (a.order || 0) - (b.order || 0)).map(mapSymptom);
}

export async function getQuizQuestions(symptomIds: Array<string | number>): Promise<TriageQuestion[]> {
  const ids = symptomIds.map((id) => toNumericId(id, 'Sintoma'));
  const response = await api.post('/assessments/questionnaire', { symptomIds: ids });
  const questionnaire = unwrapData<QuestionnaireResponse>(response.data);
  return (questionnaire.symptoms || [])
    .flatMap((symptom) => symptom.questions || [])
    .sort((a, b) => (a.order || 0) - (b.order || 0))
    .map(mapQuestion);
}

export async function submitAssessment(
  child: ChildProfile | null,
  symptom: Symptom | null,
  questions: TriageQuestion[],
  answers: TriageAnswers,
): Promise<TriageResult> {
  if (!child) throw new Error('Cadastre ou selecione uma criança antes de concluir a triagem.');
  if (!symptom) throw new Error('Selecione um sintoma antes de concluir a triagem.');

  const payloadAnswers = questions.map((question, index) => {
    const answer = answers[index];
    if (!answer) throw new Error('Responda todas as perguntas antes de concluir.');

    const questionId = toNumericId(question.backendId, 'Pergunta');
    if (question.type === 'options') {
      const option = question.options.find((item) => item.id === answer);
      return { optionId: toNumericId(option?.backendId, 'Opção'), questionId };
    }

    return { answer: mapAnswer(answer).toUpperCase(), questionId };
  });

  const response = await api.post('/assessments', {
    childId: toNumericId(child.backendId || child.id, 'Criança'),
    symptoms: [
      {
        answers: payloadAnswers,
        symptomId: toNumericId(symptom.backendId || symptom.id, 'Sintoma'),
      },
    ],
  });
  const result = unwrapData<AssessmentResultResponse>(response.data);

  return {
    answeredAt: new Date().toISOString(),
    assessmentId: result.assessmentId,
    child,
    hasRedFlag: Boolean(result.redFlagDetected),
    risk: mapClassification(result.finalClassification),
    score: result.totalScore || 0,
    symptom,
  };
}

export async function getRiskContent(): Promise<RiskContentMap> {
  return riskContent;
}

export async function getHistory(userId?: string | number | null): Promise<HistoryItem[]> {
  if (!userId) return [];

  const response = await api.get(`/assessments/users/${toNumericId(userId, 'Usuário')}/history`);
  const history = unwrapData<AssessmentHistoryResponse>(response.data);
  return (history.assessments || []).map(mapHistoryItem);
}

export async function getRecentHistory(userId?: string | number | null, size = 2): Promise<HistoryItem[]> {
  if (!userId) return [];

  const response = await api.get('/api/avaliacoes', { params: { size } });
  const history = unwrapData<AssessmentHistoryResponse>(response.data);
  return (history.assessments || []).map(mapHistoryItem);
}

export async function getOrientationCards(assessmentId?: number): Promise<OrientationCardItem[]> {
  if (!assessmentId) return orientationCards;

  const response = await api.get(`/assessments/${assessmentId}/orientation`);
  const orientation = unwrapData<OrientationResponse>(response.data);
  const cards: OrientationCardItem[] = [
    ...(orientation.mainActions || []).map((item, index) => mapOrientationItem(item, index, 'check', 'primary', 'main')),
    ...(orientation.warningSigns || []).map((item, index) => mapOrientationItem(item, index, 'warn', 'mod', 'warning')),
    ...(orientation.homeCare || []).map((item, index) => mapOrientationItem(item, index, 'pill', 'low', 'home')),
    ...(orientation.whenSeekHelp || []).map((item, index) => mapOrientationItem(item, index, 'phone', 'high', 'help')),
  ];

  return cards.length ? cards : orientationCards;
}

function mapOrientationItem(
  item: OrientationItemResponse,
  index: number,
  icon: IconName,
  tone: RiskTone,
  prefix: string,
): OrientationCardItem {
  return {
    icon,
    id: `${prefix}-${item.id || index}`,
    subtitle: item.description || item.symptomName || 'Orientação pediátrica',
    title: item.title,
    tone,
  };
}

export async function getPediatricDemoData(userId?: string | number | null): Promise<PediatricDemoData> {
  const [children, symptomList, risks, history, orientations] = await Promise.all([
    getChildren(userId),
    getSymptoms(),
    getRiskContent(),
    getHistory(userId),
    getOrientationCards(),
  ]);

  return {
    children,
    history,
    orientations,
    questions: [],
    risks,
    symptoms: symptomList,
  };
}
