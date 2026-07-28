import { View } from 'react-native';
import { EmptyStateScreen, ScreenHeader } from '../components';
import { spacing } from '../theme';
import type { IconName } from '../types/domain';

interface PreferenceDetailScreenProps {
  icon: IconName;
  message: string;
  onBack: () => void;
  title: string;
}

export function PreferenceDetailScreen({ icon, message, onBack, title }: PreferenceDetailScreenProps) {
  return (
    <View style={{ flex: 1, paddingBottom: spacing.xxl }}>
      <ScreenHeader title={title} onBack={onBack} />
      <EmptyStateScreen icon={icon} title={title} message={message || 'Em desenvolvimento'} />
    </View>
  );
}
