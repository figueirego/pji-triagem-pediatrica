import { ScrollView, Text, View } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Card, DisclaimerCard, Icon, ScreenHeader } from '../components';
import { colors, radii, spacing, typography } from '../theme';

interface AboutScreenProps {
  onBack: () => void;
}

export function AboutScreen({ onBack }: AboutScreenProps) {
  return (
    <ScrollView contentContainerStyle={{ gap: spacing.md, paddingBottom: spacing.xxl }} contentInsetAdjustmentBehavior="automatic">
      <ScreenHeader title="Sobre o aplicativo" onBack={onBack} />
      <View style={{ gap: spacing.md, paddingHorizontal: spacing.lg }}>
        <LinearGradient
          colors={[colors.navy, colors.primary]}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 1 }}
          style={{ alignItems: 'center', borderRadius: radii.hero, gap: spacing.xs, padding: spacing.xl }}
        >
          <Icon name="heart" color={colors.inverseText} size={36} />
          <Text selectable style={[typography.title, { color: colors.inverseText }]}>
            PediTriagem
          </Text>
          <Text selectable style={[typography.caption, { color: colors.inverseText }]}>
            Versão 1.0.0
          </Text>
        </LinearGradient>

        <Card contentStyle={{ gap: spacing.xs }}>
          <Text selectable style={[typography.subtitle, { color: colors.text }]}>
            Apoio à decisão para pais e cuidadores
          </Text>
          <Text selectable style={[typography.body, { color: colors.textMuted }]}>
            Ferramenta acadêmica de orientação inicial, baseada em fluxos de triagem pediátrica.
          </Text>
        </Card>

        <DisclaimerCard variant="warning" />

        {['Termos de uso', 'Política de privacidade', 'Fontes clínicas e referências', 'Equipe e créditos'].map((item) => (
          <Card key={item} padding={spacing.sm} contentStyle={{ alignItems: 'center', flexDirection: 'row', gap: spacing.sm }}>
            <Text selectable style={[typography.bodyStrong, { color: colors.text, flex: 1 }]}>
              {item}
            </Text>
            <Icon name="chevronRight" color={colors.textSubtle} size={16} />
          </Card>
        ))}
        <Text selectable style={[typography.caption, { color: colors.textSubtle, textAlign: 'center' }]}>
          © 2026 · Projeto acadêmico
        </Text>
      </View>
    </ScrollView>
  );
}
