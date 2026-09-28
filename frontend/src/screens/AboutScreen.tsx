import { Linking, ScrollView, Text, View } from 'react-native';
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

        {[
          ['Objetivo e público', 'Apoiar pais, responsáveis e cuidadores de crianças de 0 a 12 anos na avaliação inicial de nove sintomas comuns. O projeto busca ampliar a educação em saúde, diminuir a insegurança e orientar quando procurar atendimento.'],
          ['Como funciona', 'Confirme a idade, escolha o sintoma e responda às perguntas. O resultado indica atendimento imediato, avaliação nas próximas horas (até 24 horas) ou observação domiciliar. O histórico permite consultar avaliações anteriores.'],
          ['Limites e termos de uso', 'Projeto acadêmico educativo, com regras demonstrativas extraídas dos documentos do projeto. Não é um protocolo clinicamente validado, não diagnostica doenças, não prescreve medicamentos e não substitui atendimento médico. O resultado depende das respostas. Em piora ou dúvida, procure um profissional.'],
          ['Privacidade', 'Dados de cadastro, perfis infantis, respostas e resultados são associados à conta e armazenados no servidor. Compartilhe resultados somente com pessoas de confiança. Consulte Privacidade e dados no perfil para conhecer o armazenamento e os limites do protótipo.'],
          ['Equipe e créditos', 'PediTriagem — projeto acadêmico de sistema inteligente de triagem pediátrica para orientação de pais e responsáveis. Implementação baseada nos documentos de apresentação e versões básica e avançada do projeto.'],
        ].map(([title, text]) => <Card key={title} contentStyle={{gap:spacing.xs}}><Text style={[typography.subtitle,{color:colors.text}]}>{title}</Text><Text selectable style={[typography.body,{color:colors.textMuted}]}>{text}</Text></Card>)}
        <Card onPress={()=>{void Linking.openURL('https://www.nhs.uk/baby/health/when-to-get-urgent-medical-help-for-babies-and-children-under-5/');}}><Text style={[typography.bodyStrong,{color:colors.primary}]}>Referência educativa: sinais de urgência (NHS)</Text></Card>
        <Text selectable style={[typography.caption, { color: colors.textSubtle, textAlign: 'center' }]}>
          © 2026 · Projeto acadêmico
        </Text>
      </View>
    </ScrollView>
  );
}
