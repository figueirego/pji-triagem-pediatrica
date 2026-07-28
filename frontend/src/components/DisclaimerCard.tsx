import { Text, View } from 'react-native';
import { colors, spacing, typography } from '../theme';
import { Card } from './Card';
import { Icon } from './Icon';

interface DisclaimerCardProps {
  variant?: 'info' | 'warning';
}

export function DisclaimerCard({ variant = 'info' }: DisclaimerCardProps) {
  const warning = variant === 'warning';
  return (
    <Card
      padding={spacing.sm}
      style={{
        backgroundColor: warning ? colors.modSofter : colors.primarySoft,
        borderColor: warning ? colors.modSoft : colors.primarySoft,
      }}
      contentStyle={{ flexDirection: 'row', gap: spacing.sm }}
    >
      <Icon name={warning ? 'warn' : 'info'} color={warning ? colors.modSolid : colors.primary} size={18} />
      <View style={{ flex: 1 }}>
        <Text selectable style={[typography.caption, { color: warning ? colors.warningText : colors.navy }]}>
          Esta orientação apoia a decisão inicial e não substitui avaliação médica. Em emergências, ligue 192 ou procure atendimento.
        </Text>
      </View>
    </Card>
  );
}
