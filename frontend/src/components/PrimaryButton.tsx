import type { ReactNode } from 'react';
import { ActivityIndicator, Pressable, Text, View } from 'react-native';
import type { GestureResponderEvent, StyleProp, ViewStyle } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { colors, radii, spacing, typography } from '../theme';
import type { RiskLevel } from '../types/domain';

const toneColors = {
  primary: [colors.secondary, colors.primary],
  navy: [colors.navy, colors.primaryDark],
  low: [colors.lowSolid, colors.lowSolid],
  mod: [colors.modSolid, colors.modSolid],
  high: [colors.highSolid, colors.highSolid],
} satisfies Record<PrimaryButtonTone, readonly [string, string]>;

type PrimaryButtonTone = RiskLevel | 'primary' | 'navy';

interface PrimaryButtonProps {
  children: ReactNode;
  disabled?: boolean;
  full?: boolean;
  icon?: ReactNode;
  loading?: boolean;
  onPress?: (event: GestureResponderEvent) => void;
  style?: StyleProp<ViewStyle>;
  tone?: PrimaryButtonTone;
}

export function PrimaryButton({
  children,
  onPress,
  tone = 'primary',
  icon,
  full = true,
  disabled = false,
  loading = false,
  style,
}: PrimaryButtonProps) {
  const gradient = toneColors[tone];

  return (
    <Pressable
      accessibilityRole="button"
      disabled={disabled || loading}
	      onPress={onPress}
	      style={({ pressed }) => [
	        {
	          alignSelf: full ? 'stretch' : 'flex-start',
	          opacity: disabled ? 0.55 : 1,
	          transform: pressed && !disabled ? [{ scale: 0.99 }] : [],
	        },
	        style,
	      ]}
    >
      <LinearGradient
        colors={gradient}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 1 }}
        style={{
          alignItems: 'center',
          borderRadius: radii.xl,
          flexDirection: 'row',
          gap: spacing.xs,
          justifyContent: 'center',
          minHeight: 52,
          paddingHorizontal: spacing.lg,
          paddingVertical: spacing.md,
        }}
      >
        {loading ? <ActivityIndicator color={colors.inverseText} /> : icon}
        <Text selectable={false} style={[typography.subtitle, { color: colors.inverseText }]}>
          {children}
        </Text>
      </LinearGradient>
    </Pressable>
  );
}
