import { Alert, Linking, ScrollView, Share, Text, View } from 'react-native';
import { Card, DisclaimerCard, GhostButton, Icon, Mascot, Pill, PrimaryButton, ScreenHeader } from '../components';
import { colors, radii, riskPalette, spacing, typography } from '../theme';
import type { RiskContent, RiskContentMap, RiskLevel, TriageResult } from '../types/domain';
import { buildTriageShareMessage } from '../utils/frontendGaps';

interface SummaryRowProps {
  label: string;
  value: string;
  last?: boolean;
}

interface ResultScreenProps {
  onBackHome: () => void;
  onGoOrientations: () => void;
  result: TriageResult | null;
  risks?: RiskContentMap;
}

function SummaryRow({ label, value, last = false }: SummaryRowProps) {
  return (
    <View
      style={{
        borderBottomColor: colors.divider,
        borderBottomWidth: last ? 0 : 1,
        flexDirection: 'row',
        gap: spacing.sm,
        justifyContent: 'space-between',
        paddingVertical: spacing.xs,
      }}
    >
      <Text selectable style={[typography.caption, { color: colors.textMuted }]}>
        {label}
      </Text>
      <Text selectable style={[typography.caption, { color: colors.text, flex: 1, textAlign: 'right' }]}>
        {value}
      </Text>
    </View>
  );
}

export function ResultScreen({ onBackHome, onGoOrientations, result, risks }: ResultScreenProps) {
  const risk: RiskLevel = result?.risk || 'low';
  const content: RiskContent = risks?.[risk] || risks?.low || {
    actions: ['Observar sinais gerais'],
    cta: 'Ver orientações',
    icon: 'info',
    message: 'Não foi possível carregar o conteúdo detalhado deste risco.',
    mood: 'calm',
    title: 'Orientação indisponível',
  };
  const palette = riskPalette[risk];
  const dateLabel = new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    month: 'short',
  }).format(result?.answeredAt ? new Date(result.answeredAt) : new Date());

  async function handleShareResult() {
    try {
      await Share.share({
        message: buildTriageShareMessage(result, palette.label),
        title: 'Resultado da triagem pediátrica',
      });
    } catch {
      Alert.alert('Não foi possível compartilhar', 'Tente novamente em instantes.');
    }
  }

  return (
    <ScrollView contentContainerStyle={{ gap: spacing.md, paddingBottom: spacing.xxl }} contentInsetAdjustmentBehavior="automatic">
      <ScreenHeader title="Resultado da triagem" onBack={onBackHome} />
      <View style={{ gap: spacing.md, paddingHorizontal: spacing.lg }}>
        <Card
          style={{ backgroundColor: palette.softer, borderColor: palette.soft }}
          contentStyle={{ alignItems: 'center', gap: spacing.sm }}
        >
          <View
            style={{
              alignItems: 'center',
              backgroundColor: palette.soft,
              borderRadius: radii.avatar,
              height: 82,
              justifyContent: 'center',
              width: 82,
            }}
          >
            <Mascot mood={content.mood} size={58} />
          </View>
          <Pill tone={risk} withDot>
            {palette.label}
          </Pill>
          <Text selectable style={[typography.title, { color: colors.text, textAlign: 'center' }]}>
            {content.title}
          </Text>
          <Text selectable style={[typography.body, { color: colors.textMuted, textAlign: 'center' }]}>
            {content.message}
          </Text>
        </Card>

        <View style={{ gap: spacing.xs }}>
          <Text selectable style={[typography.eyebrow, { color: colors.textMuted }]}>
            O que fazer agora
          </Text>
          {content.actions.map((action, index) => (
            <Card key={action} padding={spacing.sm} contentStyle={{ alignItems: 'center', flexDirection: 'row', gap: spacing.sm }}>
              <View
                style={{
                  alignItems: 'center',
                  backgroundColor: palette.softer,
                  borderRadius: radii.sm,
                  height: 30,
                  justifyContent: 'center',
                  width: 30,
                }}
              >
                <Text selectable={false} style={[typography.caption, { color: palette.solid, fontVariant: ['tabular-nums'] }]}>
                  {index + 1}
                </Text>
              </View>
              <Text selectable style={[typography.bodyStrong, { color: colors.text, flex: 1 }]}>
                {action}
              </Text>
            </Card>
          ))}
        </View>

        <Card>
          <Text selectable style={[typography.eyebrow, { color: colors.textMuted, marginBottom: spacing.xs }]}>
            Resumo da triagem
          </Text>
          <SummaryRow label="Criança" value={`${result?.child?.name || 'Maria'}, ${result?.child?.age || '4 anos'}`} />
          <SummaryRow label="Sintoma principal" value={result?.symptom?.name || 'Febre'} />
          <SummaryRow label="Data" value={dateLabel} />
          <SummaryRow label="Pontuação" value={`${result?.score ?? 0}/10`} last />
        </Card>

        <View style={{ gap: spacing.xs }}>
          <PrimaryButton tone={risk} onPress={onGoOrientations} icon={<Icon name={risk === 'high' ? 'phone' : 'book'} color={colors.inverseText} size={18} />}>
            {content.cta}
          </PrimaryButton>
          <GhostButton onPress={() => { void handleShareResult(); }} icon={<Icon name="share" size={18} color={colors.primary} />}>
            Compartilhar resultado
          </GhostButton>
          {risk === 'high' ? (
            <GhostButton onPress={() => { void Linking.openURL('tel:192'); }} icon={<Icon name="phone" size={18} color={colors.highSolid} />}>
              Ligar para SAMU 192
            </GhostButton>
          ) : null}
          <GhostButton onPress={onBackHome}>Voltar ao início</GhostButton>
        </View>

        <DisclaimerCard />
      </View>
    </ScrollView>
  );
}
