import { ScrollView, Text, View } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Card, Icon, ScreenHeader, SymptomCard } from '../components';
import { colors, radii, riskPalette, spacing, typography } from '../theme';
import type { OrientationCardItem, RiskTone, Symptom } from '../types/domain';

interface OrientationsScreenProps {
  onBack: () => void;
  onSelectOrientation: (item: OrientationCardItem) => void;
  orientationCards?: OrientationCardItem[];
  symptoms?: Symptom[];
}

interface OrientationToneStyle {
  solid: string;
  softer: string;
}

function toneStyle(tone: RiskTone): OrientationToneStyle {
  if (tone === 'low') return riskPalette.low;
  if (tone === 'mod') return riskPalette.mod;
  if (tone === 'high') return riskPalette.high;
  return tone === 'primary'
    ? { solid: colors.primary, softer: colors.primarySoft }
    : { solid: colors.textMuted, softer: colors.neutralSoft };
}

export function OrientationsScreen({ onBack, onSelectOrientation, orientationCards = [], symptoms = [] }: OrientationsScreenProps) {
  return (
    <ScrollView contentContainerStyle={{ gap: spacing.md, paddingBottom: spacing.tabContentBottom }} contentInsetAdjustmentBehavior="automatic">
      <ScreenHeader title="Orientações" onBack={onBack} />
      <View style={{ gap: spacing.md, paddingHorizontal: spacing.lg }}>
        <LinearGradient
          colors={[colors.secondary, colors.primary]}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 1 }}
          style={{ borderRadius: radii.card, gap: spacing.xs, overflow: 'hidden', padding: spacing.lg }}
        >
          <Text selectable style={[typography.eyebrow, { color: colors.inverseText }]}>
            Guia para cuidadores
          </Text>
          <Text selectable style={[typography.title, { color: colors.inverseText }]}>
            Cuidados em casa pela faixa etária
          </Text>
          <Text selectable style={[typography.body, { color: colors.inverseText }]}>
            Conteúdo educativo para acompanhar a criança até uma avaliação quando necessário.
          </Text>
        </LinearGradient>

        <View style={{ gap: spacing.xs }}>
          {orientationCards.map((item) => {
            const tone = toneStyle(item.tone);
            return (
              <Card
                key={item.id}
                onPress={() => onSelectOrientation(item)}
                padding={spacing.sm}
                contentStyle={{ alignItems: 'center', flexDirection: 'row', gap: spacing.sm }}
              >
                <View
                  style={{
                    alignItems: 'center',
                    backgroundColor: tone.softer,
                    borderRadius: radii.lg,
                    height: 44,
                    justifyContent: 'center',
                    width: 44,
                  }}
                >
                  <Icon name={item.icon} color={tone.solid} size={22} />
                </View>
                <View style={{ flex: 1 }}>
                  <Text selectable style={[typography.bodyStrong, { color: colors.text }]}>
                    {item.title}
                  </Text>
                  <Text selectable style={[typography.caption, { color: colors.textMuted, marginTop: spacing.xxs / 2 }]}>
                    {item.subtitle}
                  </Text>
                </View>
                <Icon name="chevronRight" color={colors.textSubtle} size={18} />
              </Card>
            );
          })}
        </View>

        <View style={{ gap: spacing.xs }}>
          <Text selectable style={[typography.eyebrow, { color: colors.textMuted }]}>
            Por sintoma
          </Text>
          {symptoms.map((symptom) => (
            <SymptomCard
              key={symptom.id}
              symptom={symptom}
              onPress={() => onSelectOrientation({
                icon: symptom.icon,
                id: `symptom-${symptom.id}`,
                subtitle: symptom.desc,
                title: symptom.name,
                tone: symptom.tone,
              })}
            />
          ))}
        </View>
      </View>
    </ScrollView>
  );
}
