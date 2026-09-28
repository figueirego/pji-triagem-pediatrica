import { AgeConfirmScreen } from '../screens/AgeConfirmScreen';
import { Alert, View } from 'react-native';
import { AppTabBar, ErrorState, LoadingState } from '../components';
import { AboutScreen } from '../screens/AboutScreen';
import { ChildFormScreen } from '../screens/ChildFormScreen';
import { DevScreen } from '../screens/DevScreen';
import { HistoryScreen } from '../screens/HistoryScreen';
import { OrientationDetailScreen } from '../screens/OrientationDetailScreen';
import { HomeScreen } from '../screens/HomeScreen';
import { OrientationsScreen } from '../screens/OrientationsScreen';
import { PreferenceDetailScreen } from '../screens/PreferenceDetailScreen';
import { ProfileScreen } from '../screens/ProfileScreen';
import { QuizScreen } from '../screens/QuizScreen';
import { ResultScreen } from '../screens/ResultScreen';
import { SymptomsScreen } from '../screens/SymptomsScreen';
import { colors } from '../theme';
import { useAuth } from '../hooks/useAuth';
import { usePediatricData } from '../hooks/usePediatricData';
import { useEffect, useMemo, useState } from 'react';
import {
  createChild,
  deleteChild,
  getAssessmentResult,
  getOrientationCards,
  getQuizQuestions,
  submitAssessment,
  updateChild,
} from '../services/pediatricService';
import { getApiErrorMessage } from '../services/api';
import type {
  AppScreen,
  AppTab,
  ChildProfile,
  HistoryItem,
  OrientationCardItem,
  Symptom,
  TriageAnswers,
  TriageQuestion,
  TriageResult,
} from '../types/domain';

const tabToScreen: Record<AppTab, AppScreen> = {
  home: 'home',
  evaluate: 'age-confirm',
  orientations: 'orientations',
  history: 'history',
  profile: 'profile',
};

function tabForScreen(screen: AppScreen): AppTab {
  if (['age-confirm', 'symptoms', 'quiz', 'result'].includes(screen)) return 'evaluate';
  if (screen === 'orientations' || screen === 'orientation-detail') return 'orientations';
  if (screen === 'history') return 'history';
  if (['profile', 'about', 'child-add', 'child-edit', 'dev', 'notifications', 'privacy'].includes(screen)) return 'profile';
  return 'home';
}

export function MainTabs() {
  const { user } = useAuth();
  const { data, error, isLoading, reload } = usePediatricData(user?.id);
  const [childrenList, setChildrenList] = useState<ChildProfile[]>([]);
  const [historyItems, setHistoryItems] = useState<HistoryItem[]>([]);
  const [orientationCards, setOrientationCards] = useState<OrientationCardItem[]>([]);
  const [quizQuestions, setQuizQuestions] = useState<TriageQuestion[]>([]);
  const [quizError, setQuizError] = useState<string | null>(null);
  const [isQuizLoading, setIsQuizLoading] = useState(false);
  const [selectedChild, setSelectedChild] = useState<ChildProfile | null>(null);
  const [editingChild, setEditingChild] = useState<ChildProfile | null>(null);
  const [selectedOrientation, setSelectedOrientation] = useState<OrientationCardItem | null>(null);
  const [selectedSymptom, setSelectedSymptom] = useState<Symptom | null>(null);
  const [result, setResult] = useState<TriageResult | null>(null);
  const [screen, setScreen] = useState<AppScreen>('home');
  const [returnScreen, setReturnScreen] = useState<AppScreen>('home');

  const activeTab = useMemo(() => tabForScreen(screen), [screen]);
  const showTabs = !['child-add', 'child-edit', 'quiz', 'result', 'about', 'dev'].includes(screen);

  useEffect(() => {
    setChildrenList(data.children);
    setSelectedChild((current) => {
      if (current) { const refreshed = data.children.find((child) => child.id === current.id); if (refreshed) return refreshed; }
      return data.children[0] || null;
    });
  }, [data.children]);

  useEffect(() => {
    if (data.symptoms.length) {
      setSelectedSymptom((current) => current || data.symptoms[0] || null);
    }
  }, [data.symptoms]);

  useEffect(() => {
    setHistoryItems(data.history);
  }, [data.history]);

  useEffect(() => {
    setOrientationCards(data.orientations);
  }, [data.orientations]);

  function go(nextScreen: AppScreen) {
    if (nextScreen === 'about') setReturnScreen(screen);
    if (nextScreen === 'child-add') {
      setEditingChild(null);
      setReturnScreen(screen);
    }
    setScreen(nextScreen === 'symptoms' ? 'age-confirm' : nextScreen);
  }

  function changeTab(tab: AppTab) {
    setScreen(tabToScreen[tab]);
  }

  function handleSelectSymptom(symptom: Symptom) {
    setSelectedSymptom(symptom);
    setQuizQuestions([]);
    setQuizError(null);
    setIsQuizLoading(true);
    setScreen('quiz');

    getQuizQuestions([symptom.backendId || symptom.id])
      .then(setQuizQuestions)
      .catch((loadError) => setQuizError(getApiErrorMessage(loadError, 'Não foi possível carregar o questionário.')))
      .finally(() => setIsQuizLoading(false));
  }

  function handleSelectOrientation(item: OrientationCardItem) {
    setSelectedOrientation(item);
    setScreen('orientation-detail');
  }

  function handleEditChild(child: ChildProfile) {
    setEditingChild(child);
    setReturnScreen(screen);
    setScreen('child-edit');
  }

  async function handleSaveChild(child: ChildProfile) {
    if (!user?.id) throw new Error('Entre novamente antes de cadastrar uma criança.');

    const savedChild = await createChild(user.id, child);
    setChildrenList((current) => [...current.filter((item) => item.id !== savedChild.id), savedChild]);
    setSelectedChild(savedChild);
    setScreen(returnScreen === 'age-confirm' ? 'age-confirm' : 'profile');
  }

  async function handleUpdateChild(child: ChildProfile) {
    if (!user?.id) throw new Error('Entre novamente antes de editar uma criança.');

    const savedChild = await updateChild(user.id, child);
    setChildrenList((current) => current.map((item) => (item.id === child.id ? savedChild : item)));
    setSelectedChild((current) => (current?.id === child.id ? savedChild : current));
    setEditingChild(null);
    setScreen(returnScreen === 'age-confirm' ? 'age-confirm' : 'profile');
  }

  async function handleDeleteChild(child: ChildProfile) {
    if (!user?.id) throw new Error('Entre novamente antes de excluir uma criança.');

    await deleteChild(user.id, child.backendId || child.id);
    const nextChildren = childrenList.filter((item) => item.id !== child.id);
    setChildrenList(nextChildren);
    setSelectedChild((current) => (current?.id === child.id ? nextChildren[0] || null : current));
    setEditingChild(null);
    setScreen(returnScreen === 'age-confirm' ? 'age-confirm' : 'profile');
  }

  async function handleFinishQuiz(answers: TriageAnswers) {
    const nextResult = await submitAssessment(selectedChild, selectedSymptom, quizQuestions, answers);
    setResult(nextResult);
    setHistoryItems((current) => [
      {
        childId: selectedChild?.id,
        child: nextResult.child?.name || 'Criança',
        childAgeAtAssessment: nextResult.child?.age,
        reason: nextResult.reason,
        protocolVersion: nextResult.protocolVersion,
        date: 'Agora',
        id: String(nextResult.assessmentId || Date.now()),
        risk: nextResult.risk,
        symptom: selectedSymptom?.name || 'Sintoma',
      },
      ...current,
    ]);
    try {
      setOrientationCards(await getOrientationCards(nextResult.assessmentId));
    } catch {
      setOrientationCards([]);
      Alert.alert('Orientações indisponíveis', 'O resultado foi salvo. Consulte-o no histórico para tentar carregar novamente as orientações.');
    }
    setScreen('result');
  }

  async function handleOpenAssessment(item: HistoryItem) {
    try {
      const saved = await getAssessmentResult(item, childrenList, data.symptoms);
      setResult(saved);
      setOrientationCards([]);
      setScreen('result');
      try {
        setOrientationCards(await getOrientationCards(saved.assessmentId));
      } catch {
        Alert.alert('Orientações indisponíveis', 'O resultado está disponível. Abra novamente a avaliação no histórico para tentar carregar as orientações.');
      }
    } catch (error) { Alert.alert('Avaliação indisponível', getApiErrorMessage(error, 'Não foi possível carregar. Tente novamente.')); }
  }

  function renderScreen() {
    if (isLoading) {
      return (
        <View style={{ flex: 1, justifyContent: 'center' }}>
          <LoadingState title="Carregando dados" message="Preparando sintomas, histórico e orientações." />
        </View>
      );
    }

    if (error) {
      return (
        <View style={{ flex: 1, justifyContent: 'center' }}>
          <ErrorState message={error} onRetry={reload} />
        </View>
      );
    }

    if (screen === 'child-add') {
      return <ChildFormScreen onBack={() => setScreen(returnScreen)} onSave={handleSaveChild} />;
    }

    if (screen === 'child-edit') {
      if (!editingChild) {
        return <ProfileScreen childrenList={childrenList} onEditChild={handleEditChild} onGo={go} />;
      }

      return (
        <ChildFormScreen
          initialChild={editingChild}
          onBack={() => {
            setEditingChild(null);
            setScreen(returnScreen);
          }}
          onDelete={handleDeleteChild}
          onSave={handleUpdateChild}
        />
      );
    }

    if (screen === 'age-confirm') {
      return <AgeConfirmScreen childrenList={childrenList} selectedChild={selectedChild} onSelect={setSelectedChild} onBack={()=>setScreen('home')} onAdd={()=>go('child-add')} onEdit={handleEditChild} onContinue={()=>{ if(selectedChild) setScreen('symptoms'); }} />;
    }
    if (screen === 'symptoms') {
      return (
        <SymptomsScreen
          onBack={() => setScreen('home')}
          onSelectSymptom={handleSelectSymptom}
          selectedChild={selectedChild}
          symptoms={data.symptoms}
        />
      );
    }

    if (screen === 'quiz') {
      if (isQuizLoading) {
        return (
          <View style={{ flex: 1, justifyContent: 'center' }}>
            <LoadingState title="Carregando questionário" message="Buscando perguntas para o sintoma selecionado." />
          </View>
        );
      }

      if (quizError) {
        return (
          <View style={{ flex: 1, justifyContent: 'center' }}>
            <ErrorState
              message={quizError}
              onRetry={() => {
                if (selectedSymptom) handleSelectSymptom(selectedSymptom);
              }}
            />
          </View>
        );
      }

      return (
        <QuizScreen
          onBack={() => setScreen('symptoms')}
          onFinish={handleFinishQuiz}
          questions={quizQuestions}
          selectedChild={selectedChild}
          selectedSymptom={selectedSymptom}
        />
      );
    }

    if (screen === 'result') {
      return <ResultScreen onBackHome={() => setScreen('home')} onGoOrientations={() => setScreen('orientations')} result={result} risks={data.risks} orientations={orientationCards} />;
    }

    if (screen === 'orientations') {
      return (
        <OrientationsScreen
          onBack={() => setScreen('home')}
          onSelectOrientation={handleSelectOrientation}
          orientationCards={orientationCards}
          symptoms={data.symptoms}
        />
      );
    }

    if (screen === 'orientation-detail') {
      if (selectedOrientation) {
        return <OrientationDetailScreen item={selectedOrientation} onBack={() => setScreen('orientations')} />;
      }

      return (
        <OrientationsScreen
          onBack={() => setScreen('home')}
          onSelectOrientation={handleSelectOrientation}
          orientationCards={orientationCards}
          symptoms={data.symptoms}
        />
      );
    }

    if (screen === 'history') {
      return <HistoryScreen childrenList={childrenList} historyItems={historyItems} onSelectAssessment={handleOpenAssessment} onBack={() => setScreen('home')} onRefresh={reload} />;
    }

    if (screen === 'profile') {
      return <ProfileScreen childrenList={childrenList} onEditChild={handleEditChild} onGo={go} />;
    }

    if (screen === 'about') {
      return <AboutScreen onBack={() => setScreen(returnScreen)} />;
    }

    if (screen === 'notifications') {
      return (
        <PreferenceDetailScreen
          icon="bell"
          message="Não há notificações automáticas neste protótipo. Em caso de sinais de alerta ou piora, procure atendimento sem aguardar um aviso do aplicativo."
          onBack={() => setScreen('profile')}
          title="Notificações"
        />
      );
    }

    if (screen === 'privacy') {
      return (
        <PreferenceDetailScreen
          icon="shield"
          message="No aplicativo nativo, o token de sessão fica no armazenamento seguro do sistema. Na web, fica na sessão da aba e continua acessível a scripts da página. O cadastro armazena nome, e-mail e identificação do responsável; os perfis guardam nome, nascimento e peso opcional da criança. As respostas e resultados ficam associados à sua conta no servidor para consultar o histórico. Use dados fictícios na demonstração acadêmica. O botão Compartilhar envia o resumo, inclusive nome e sintomas, somente ao destino escolhido por você. Sair encerra a sessão neste dispositivo, mas não apaga o histórico do servidor. A exclusão de uma criança remove seu perfil e avaliações vinculadas. Este protótipo não oferece ainda exportação ou exclusão completa da conta; esses recursos e uma política de retenção precisam ser definidos antes de uso público."
          onBack={() => setScreen('profile')}
          title="Privacidade e dados"
        />
      );
    }

    if (screen === 'dev') {
      return <DevScreen onBack={() => setScreen('profile')} />;
    }

    return (
      <HomeScreen
        onSelectAssessment={handleOpenAssessment}
        childrenList={childrenList}
        historyItems={historyItems}
        onGo={go}
        onSelectChild={setSelectedChild}
        selectedChild={selectedChild}
        user={user}
      />
    );
  }

  return (
    <View style={{ backgroundColor: colors.background, flex: 1 }}>
      {renderScreen()}
      {showTabs ? <AppTabBar activeTab={activeTab} onChange={changeTab} /> : null}
    </View>
  );
}
