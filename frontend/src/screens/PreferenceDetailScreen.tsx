import { ScrollView, Text, View } from 'react-native';
import { Card, ScreenHeader } from '../components';
import { colors, typography, spacing } from '../theme';
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
      <ScrollView contentContainerStyle={{padding:spacing.lg}}><Card><Text selectable style={[typography.body,{color:colors.text}]}>{message}</Text></Card></ScrollView>
    </View>
  );
}
