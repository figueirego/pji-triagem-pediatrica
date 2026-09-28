import { ScrollView, Text, View } from 'react-native';
import { Card, PrimaryButton, GhostButton, ScreenHeader } from '../components';
import { colors, spacing, typography } from '../theme';
import type { ChildProfile } from '../types/domain';

interface AgeConfirmScreenProps {
  childrenList: ChildProfile[];
  selectedChild: ChildProfile | null;
  onSelect: (child: ChildProfile) => void;
  onContinue: () => void;
  onAdd: () => void;
  onEdit: (child: ChildProfile) => void;
  onBack: () => void;
}

export function AgeConfirmScreen({ childrenList, selectedChild, onSelect, onContinue, onAdd, onEdit, onBack }: AgeConfirmScreenProps) {
  return (
    <ScrollView contentContainerStyle={{ gap: spacing.md, paddingBottom: spacing.tabContentBottom }}>
      <ScreenHeader title="Confirmar idade" onBack={onBack} />
      <View style={{ paddingHorizontal: spacing.lg, gap: spacing.md }}>
        <Text style={[typography.body, { color: colors.text }]}>
          Selecione a criança e confira os dados cadastrados. A idade é calculada automaticamente pela data de nascimento e usada nas condições de idade da triagem, sem precisar responder novamente.
        </Text>
        {childrenList.map((child) => (
          <Card
            key={child.id}
            onPress={() => onSelect(child)}
            style={{ borderColor: selectedChild?.id === child.id ? colors.primary : colors.hairline }}
          >
            <Text style={[typography.subtitle, { color: colors.text }]}>{child.name} · {child.age}</Text>
            {child.birthDate ? (
              <Text style={[typography.caption, { color: colors.textMuted }]}>
                Nascimento: {child.birthDate.split('-').reverse().join('/')}
              </Text>
            ) : null}
          </Card>
        ))}
        <PrimaryButton disabled={!selectedChild} onPress={onContinue}>Confirmar idade e continuar</PrimaryButton>
        {selectedChild ? <GhostButton onPress={() => onEdit(selectedChild)}>Corrigir dados da criança</GhostButton> : null}
        <GhostButton onPress={onAdd}>Cadastrar criança</GhostButton>
      </View>
    </ScrollView>
  );
}
