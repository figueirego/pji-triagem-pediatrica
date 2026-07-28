import { Pressable, ScrollView, Text, View } from 'react-native';
import { Card, Icon, ScreenHeader } from '../components';
import { colors, radii, riskPalette, spacing, typography } from '../theme';
import { useAuth } from '../hooks/useAuth';
import type { AppScreen, ChildProfile, PreferenceItem } from '../types/domain';

interface ProfileScreenProps {
  childrenList: ChildProfile[];
  onEditChild: (child: ChildProfile) => void;
  onGo: (screen: AppScreen) => void;
}

function tintFor(child: ChildProfile) {
  if (child.tint === 'low') return riskPalette.low.softer;
  if (child.tint === 'mod') return riskPalette.mod.softer;
  if (child.tint === 'high') return riskPalette.high.softer;
  return colors.primarySoft;
}

const preferences: PreferenceItem[] = [
  { icon: 'bell', label: 'Notificações', go: 'notifications' },
  { icon: 'shield', label: 'Privacidade e dados', go: 'privacy' },
  { icon: 'info', label: 'Sobre o aplicativo', go: 'about' },
  { icon: 'settings', label: 'Storybook visual', go: 'dev' },
];

export function ProfileScreen({ childrenList, onEditChild, onGo }: ProfileScreenProps) {
  const { logout, user } = useAuth();
  const initials = user?.name
    ?.split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0))
    .join('')
    .toUpperCase() || 'PT';

  return (
    <ScrollView contentContainerStyle={{ gap: spacing.md, paddingBottom: spacing.tabContentBottom }} contentInsetAdjustmentBehavior="automatic">
      <ScreenHeader title="Perfil" />
      <View style={{ gap: spacing.md, paddingHorizontal: spacing.lg }}>
        <Card contentStyle={{ alignItems: 'center', flexDirection: 'row', gap: spacing.md }}>
          <View
            style={{
              alignItems: 'center',
              backgroundColor: colors.primary,
              borderRadius: radii.xxl,
              height: 56,
              justifyContent: 'center',
              width: 56,
            }}
          >
            <Text selectable={false} style={[typography.subtitle, { color: colors.inverseText }]}>
              {initials}
            </Text>
          </View>
          <View style={{ flex: 1 }}>
            <Text selectable style={[typography.subtitle, { color: colors.text }]}>
              {user?.name || 'Cuidador'}
            </Text>
            <Text selectable style={[typography.caption, { color: colors.textMuted, marginTop: spacing.xxs / 2 }]}>
              {childrenList.length} crianças cadastradas
            </Text>
          </View>
          <Icon name="chevronRight" color={colors.textSubtle} size={18} />
        </Card>

        <View style={{ gap: spacing.xs }}>
          <Text selectable style={[typography.eyebrow, { color: colors.textMuted }]}>
            Crianças
          </Text>
          {childrenList.map((child) => (
            <Card
              key={child.id}
              onPress={() => onEditChild(child)}
              padding={spacing.sm}
              contentStyle={{ alignItems: 'center', flexDirection: 'row', gap: spacing.sm }}
            >
              <View
                style={{
                  alignItems: 'center',
                  backgroundColor: tintFor(child),
                  borderRadius: radii.lg,
                  height: 42,
                  justifyContent: 'center',
                  width: 42,
                }}
              >
                <Text selectable={false} style={[typography.caption, { color: colors.navy }]}>
                  {child.initials}
                </Text>
              </View>
              <View style={{ flex: 1 }}>
                <Text selectable style={[typography.bodyStrong, { color: colors.text }]}>
                  {child.name}
                </Text>
                <Text selectable style={[typography.caption, { color: colors.textMuted }]}>
                  {child.age} · {child.weight}
                </Text>
              </View>
              <Icon name="chevronRight" color={colors.textSubtle} size={16} />
            </Card>
          ))}
          <Pressable
            accessibilityRole="button"
            onPress={() => onGo('child-add')}
            style={{
              alignItems: 'center',
              borderColor: colors.hairline,
              borderRadius: radii.lg,
              borderStyle: 'dashed',
              borderWidth: 1.5,
              flexDirection: 'row',
              gap: spacing.xs,
              justifyContent: 'center',
              padding: spacing.md,
            }}
          >
            <Icon name="plus" color={colors.primary} size={16} />
            <Text selectable={false} style={[typography.bodyStrong, { color: colors.primary }]}>
              Adicionar criança
            </Text>
          </Pressable>
        </View>

        <View style={{ gap: spacing.xs }}>
          <Text selectable style={[typography.eyebrow, { color: colors.textMuted }]}>
            Preferências
          </Text>
          {preferences.map((item) => (
            <Card key={item.label} onPress={item.go ? () => onGo(item.go as AppScreen) : undefined} padding={spacing.sm} contentStyle={{ alignItems: 'center', flexDirection: 'row', gap: spacing.sm }}>
              <Icon name={item.icon} color={colors.primary} size={20} />
              <Text selectable style={[typography.bodyStrong, { color: colors.text, flex: 1 }]}>
                {item.label}
              </Text>
              <Icon name="chevronRight" color={colors.textSubtle} size={16} />
            </Card>
          ))}
          <Card onPress={() => { void logout(); }} padding={spacing.sm} contentStyle={{ alignItems: 'center', flexDirection: 'row', gap: spacing.sm }}>
            <Icon name="logout" color={colors.highSolid} size={20} />
            <Text selectable={false} style={[typography.bodyStrong, { color: colors.highSolid, flex: 1 }]}>
              Sair
            </Text>
          </Card>
        </View>
      </View>
    </ScrollView>
  );
}
