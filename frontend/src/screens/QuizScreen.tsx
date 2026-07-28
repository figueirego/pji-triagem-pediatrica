import { useState } from 'react';
import { Pressable, ScrollView, Text, View } from 'react-native';
import { Card, EmptyState, Icon, Mascot, ProgressBar, ScreenHeader } from '../components';
import { colors, radii, shadows, spacing, typography } from '../theme';
import type { ChildProfile, Symptom, TriageAnswers, TriageQuestion, YesNoAnswer } from '../types/domain';

interface QuizScreenProps {
  onBack: () => void;
  onFinish: (answers: TriageAnswers) => Promise<void> | void;
  questions?: TriageQuestion[] | null;
  selectedChild: ChildProfile | null;
  selectedSymptom: Symptom | null;
}

const yesNoOptions: { id: YesNoAnswer; label: string; wide?: boolean }[] = [
  { id: 'yes', label: 'Sim' },
  { id: 'no', label: 'Não' },
  { id: 'dunno', label: 'Não tenho certeza', wide: true },
];

export function QuizScreen({ onBack, onFinish, questions = [], selectedChild, selectedSymptom }: QuizScreenProps) {
  const [step, setStep] = useState(0);
  const [answers, setAnswers] = useState<TriageAnswers>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const safeQuestions = Array.isArray(questions) ? questions : [];
  const total = safeQuestions.length;
  const current = safeQuestions[step];
  const selectedAnswer = answers[step];

  if (!current) {
    return (
      <ScrollView contentContainerStyle={{ flexGrow: 1 }} contentInsetAdjustmentBehavior="automatic">
        <ScreenHeader title="Triagem" onBack={onBack} />
        <EmptyState title="Questionário indisponível" message="Não há perguntas configuradas para este sintoma." />
      </ScrollView>
    );
  }

  function selectAnswer(value: string) {
    if (isSubmitting) return;

    const nextAnswers = { ...answers, [step]: value };
    setAnswers(nextAnswers);
    setSubmitError(null);

    setTimeout(() => {
      if (step + 1 < total) {
        setStep((currentStep) => currentStep + 1);
        return;
      }

      setIsSubmitting(true);
      Promise.resolve(onFinish(nextAnswers))
        .catch((error) => {
          setSubmitError(error instanceof Error ? error.message : 'Não foi possível concluir a triagem.');
        })
        .finally(() => setIsSubmitting(false));
    }, 140);
  }

  function goBack() {
    if (step > 0) {
      setStep((currentStep) => currentStep - 1);
      return;
    }
    onBack();
  }

  return (
    <ScrollView contentContainerStyle={{ flexGrow: 1, paddingBottom: spacing.xl }} contentInsetAdjustmentBehavior="automatic">
      <ScreenHeader
        title={selectedSymptom?.name || 'Triagem'}
        subtitle={selectedChild ? `${selectedChild.name} · ${selectedChild.age}` : undefined}
        onBack={goBack}
      />
      <ProgressBar value={step + 1} total={total} />

      <View style={{ flex: 1, gap: spacing.lg, paddingHorizontal: spacing.lg, paddingTop: spacing.xs }}>
        <View style={{ alignItems: 'flex-start', flexDirection: 'row', gap: spacing.sm }}>
          <Mascot size={46} />
          <Card
            padding={spacing.sm}
            style={{ borderTopLeftRadius: radii.xxs, flex: 1 }}
            contentStyle={{ gap: spacing.xs }}
          >
            <Text selectable style={[typography.subtitle, { color: colors.text }]}>
              {current.q}
            </Text>
            {current.sub ? (
              <Text selectable style={[typography.body, { color: colors.textMuted }]}>
                {current.sub}
              </Text>
            ) : null}
          </Card>
        </View>

        {current.type === 'options' ? (
          <View style={{ gap: spacing.xs }}>
            {current.options.map((option) => {
              const selected = selectedAnswer === option.id;
              return (
                <Pressable
                  accessibilityRole="button"
                  disabled={isSubmitting}
                  key={option.id}
                  onPress={() => selectAnswer(option.id)}
                  style={[
                    {
                      alignItems: 'center',
                      backgroundColor: selected ? colors.primary : colors.surface,
                      borderRadius: radii.xl,
                      flexDirection: 'row',
                      justifyContent: 'space-between',
                      minHeight: 56,
                      paddingHorizontal: spacing.md,
                    },
                    !selected && shadows.card,
                  ]}
                >
                  <Text selectable={false} style={[typography.bodyStrong, { color: selected ? colors.inverseText : colors.text, flex: 1 }]}>
                    {option.label}
                  </Text>
                  {selected ? <Icon name="check" color={colors.inverseText} size={18} /> : null}
                </Pressable>
              );
            })}
          </View>
        ) : (
          <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: spacing.xs }}>
            {yesNoOptions.map((option) => {
              const selected = selectedAnswer === option.id;
              return (
                <Pressable
                  accessibilityRole="button"
                  disabled={isSubmitting}
                  key={option.id}
                  onPress={() => selectAnswer(option.id)}
                  style={[
                    {
                      alignItems: 'center',
                      backgroundColor: selected ? colors.primary : colors.surface,
                      borderColor: option.wide ? colors.hairline : colors.surface,
                      borderRadius: radii.xl,
                      borderWidth: option.wide ? 1.5 : 0,
                      flexBasis: option.wide ? '100%' : '48%',
                      flexGrow: option.wide ? 1 : 0,
                      justifyContent: 'center',
                      minHeight: option.wide ? 50 : 76,
                      paddingHorizontal: spacing.md,
                    },
                    !selected && shadows.card,
                  ]}
                >
                  <Text selectable={false} style={[typography.subtitle, { color: selected ? colors.inverseText : colors.text, textAlign: 'center' }]}>
                    {option.label}
                  </Text>
                </Pressable>
              );
            })}
          </View>
        )}

        <Text selectable style={[typography.caption, { color: colors.textSubtle, textAlign: 'center' }]}>
          {isSubmitting ? 'Calculando classificação...' : 'Suas respostas serão usadas para calcular a classificação.'}
        </Text>
        <Text selectable style={[typography.caption, { color: colors.textSubtle, textAlign: 'center' }]}>
          As respostas ficam vinculadas ao usuário autenticado e são usadas apenas para orientar esta triagem.
        </Text>
        {submitError ? (
          <Text selectable style={[typography.caption, { color: colors.dangerText, textAlign: 'center' }]}>
            {submitError}
          </Text>
        ) : null}
      </View>
    </ScrollView>
  );
}
