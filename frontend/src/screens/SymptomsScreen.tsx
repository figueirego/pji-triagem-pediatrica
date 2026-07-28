import { useMemo, useState } from 'react';
import { Linking, ScrollView, Text, View } from 'react-native';
import { Card, EmptyState, GhostButton, Icon, Mascot, ScreenHeader, SymptomCard, TextField } from '../components';
import { colors, spacing, typography } from '../theme';
import type { ChildProfile, Symptom } from '../types/domain';
import { filterSymptoms } from '../utils/frontendGaps';

interface SymptomsScreenProps {
  onBack: () => void;
  onSelectSymptom: (symptom: Symptom) => void;
  selectedChild: ChildProfile | null;
  symptoms?: Symptom[];
}

export function SymptomsScreen({ onBack, onSelectSymptom, selectedChild, symptoms = [] }: SymptomsScreenProps) {
  const [query, setQuery] = useState('');
  const filteredSymptoms = useMemo(() => filterSymptoms(symptoms, query), [query, symptoms]);

  return (
    <ScrollView contentContainerStyle={{ gap: spacing.md, paddingBottom: spacing.tabContentBottom }} contentInsetAdjustmentBehavior="automatic">
      <ScreenHeader
        title="Selecionar sintoma"
        subtitle={selectedChild ? `${selectedChild.name} · ${selectedChild.age}` : undefined}
        onBack={onBack}
      />
      <View style={{ alignItems: 'center', flexDirection: 'row', gap: spacing.md, paddingHorizontal: spacing.lg }}>
        <Mascot size={54} />
        <View style={{ flex: 1 }}>
          <Text selectable style={[typography.subtitle, { color: colors.text }]}>
            Qual é o principal sintoma agora?
          </Text>
          <Text selectable style={[typography.body, { color: colors.textMuted, marginTop: spacing.xxs }]}>
            Escolha o sinal que mais preocupa para iniciar a triagem guiada.
          </Text>
        </View>
      </View>

      <View style={{ gap: spacing.sm, paddingHorizontal: spacing.lg }}>
        <TextField
          accessibilityLabel="Buscar sintomas"
          autoCapitalize="none"
          label="Buscar sintomas"
          onChangeText={setQuery}
          placeholder="Digite febre, tosse, respiração..."
          returnKeyType="search"
          value={query}
        />
        <Card
          padding={spacing.sm}
          style={{ backgroundColor: colors.highSofter, borderColor: colors.highSoft }}
          contentStyle={{ gap: spacing.sm }}
        >
          <View style={{ alignItems: 'center', flexDirection: 'row', gap: spacing.sm }}>
            <Icon name="alert" color={colors.highSolid} size={22} />
            <View style={{ flex: 1 }}>
              <Text selectable style={[typography.bodyStrong, { color: colors.text }]}>
                Emergência agora?
              </Text>
              <Text selectable style={[typography.caption, { color: colors.textMuted, marginTop: spacing.xxs / 2 }]}>
                Falta de ar intensa, sonolência extrema, convulsão ou lábios arroxeados pedem atendimento imediato.
              </Text>
            </View>
          </View>
          <GhostButton
            onPress={() => {
              void Linking.openURL('tel:192');
            }}
            icon={<Icon name="phone" size={18} color={colors.highSolid} />}
          >
            Ligar para SAMU 192
          </GhostButton>
        </Card>
      </View>

      <View style={{ gap: spacing.xs, paddingHorizontal: spacing.lg }}>
        {filteredSymptoms.length ? (
          filteredSymptoms.map((symptom) => (
            <SymptomCard key={symptom.id} symptom={symptom} onPress={() => onSelectSymptom(symptom)} />
          ))
        ) : symptoms.length ? (
          <EmptyState title="Nenhum sintoma encontrado" message="Tente buscar por outro nome ou descrição." />
        ) : (
          <EmptyState title="Nenhum sintoma disponível" message="Tente novamente quando o catálogo for carregado." />
        )}
      </View>
    </ScrollView>
  );
}
