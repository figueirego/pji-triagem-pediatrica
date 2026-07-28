export type RiskLevel = 'low' | 'mod' | 'high';
export type RiskTone = RiskLevel | 'primary' | 'neutral';
export type AgeUnit = 'meses' | 'anos';
export type MascotMood = 'calm' | 'watch' | 'alert';
export type YesNoAnswer = 'yes' | 'no' | 'dunno';
export type IconName =
  | 'activity'
  | 'alert'
  | 'back'
  | 'baby'
  | 'bandage'
  | 'bell'
  | 'belly'
  | 'book'
  | 'check'
  | 'chevronRight'
  | 'clipboard'
  | 'close'
  | 'cough'
  | 'drop'
  | 'ear'
  | 'heart'
  | 'history'
  | 'home'
  | 'info'
  | 'lung'
  | 'lock'
  | 'logout'
  | 'phone'
  | 'pill'
  | 'plus'
  | 'profile'
  | 'rash'
  | 'search'
  | 'settings'
  | 'share'
  | 'shield'
  | 'stetho'
  | 'thermo'
  | 'trauma'
  | 'userPlus'
  | 'vomit'
  | 'warn';
export type AppTab = 'home' | 'evaluate' | 'orientations' | 'history' | 'profile';
export type AppScreen =
  | 'home'
  | 'symptoms'
  | 'quiz'
  | 'result'
  | 'orientations'
  | 'orientation-detail'
  | 'history'
  | 'notifications'
  | 'profile'
  | 'privacy'
  | 'about'
  | 'child-add'
  | 'child-edit'
  | 'dev';

export interface ChildProfile {
  id: string;
  backendId?: number;
  name: string;
  ageValue?: string;
  ageUnit?: AgeUnit;
  age: string;
  weight: string;
  initials: string;
  tint: RiskTone;
  avatarEmoji?: string;
}

export interface Symptom {
  id: string;
  backendId?: number;
  code?: string;
  colorHex?: string;
  name: string;
  desc: string;
  icon: IconName;
  tone: RiskTone;
}

export interface QuizOption {
  id: string;
  backendId?: number;
  label: string;
  risk?: number;
}

export interface OptionsQuestion {
  backendId?: number;
  q: string;
  sub?: string;
  type: 'options';
  options: QuizOption[];
  redFlag?: boolean;
}

export interface YesNoQuestion {
  backendId?: number;
  q: string;
  sub?: string;
  type: 'yesno';
  weights: Record<YesNoAnswer, number>;
  redFlag?: boolean;
}

export type TriageQuestion = OptionsQuestion | YesNoQuestion;
export type TriageAnswers = Record<number, string>;

export interface TriageResult {
  assessmentId?: number;
  risk: RiskLevel;
  score: number;
  hasRedFlag: boolean;
  child?: ChildProfile | null;
  symptom?: Symptom | null;
  answeredAt?: string;
}

export interface RiskContent {
  title: string;
  message: string;
  cta: string;
  icon: IconName;
  mood: MascotMood;
  actions: string[];
}

export type RiskContentMap = Record<RiskLevel, RiskContent>;

export interface HistoryItem {
  id: string;
  child: string;
  symptom: string;
  date: string;
  risk: RiskLevel;
}

export interface OrientationCardItem {
  id: string;
  title: string;
  subtitle: string;
  icon: IconName;
  tone: RiskTone;
}

export interface PreferenceItem {
  icon: string;
  label: string;
  go?: AppScreen;
}

export interface PediatricDemoData {
  children: ChildProfile[];
  history: HistoryItem[];
  orientations: OrientationCardItem[];
  questions: TriageQuestion[];
  risks: RiskContentMap;
  symptoms: Symptom[];
}
