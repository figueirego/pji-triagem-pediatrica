import { Pressable, ScrollView, Text, View } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Card, DisclaimerCard, HistoryRow, Icon, Mascot, SectionTitle } from '../components';
import { colors, radii, riskPalette, shadows, spacing, typography } from '../theme';
import type { AuthUser } from '../types/auth';
import type { AppScreen, ChildProfile, HistoryItem, RiskTone } from '../types/domain';

interface HomeScreenProps {
  childrenList: ChildProfile[];
  historyItems?: HistoryItem[];
  onGo: (screen: AppScreen) => void;
  onSelectChild: (child: ChildProfile) => void;
  selectedChild: ChildProfile | null;
  user: AuthUser | null;
}

function getTint(tint: RiskTone) {
  if (tint === 'low') return riskPalette.low.softer;
  if (tint === 'primary') return colors.primarySoft;
  return colors.neutralSoft;
}

function getTodayLabel() {
  return new Intl.DateTimeFormat('pt-BR', { weekday: 'long', day: 'numeric', month: 'short' }).format(new Date());
}

export function HomeScreen({ childrenList, historyItems = [], onGo, onSelectChild, selectedChild, user }: HomeScreenProps) {
  const firstName = user?.name?.split(' ')?.[0] || 'Cuidador';

  return (
    <ScrollView
      contentContainerStyle={{ gap: spacing.md, padding: spacing.lg, paddingBottom: spacing.tabContentBottom, paddingTop: spacing.sm }}
      contentInsetAdjustmentBehavior="automatic"
    >
      <View style={{ alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between' }}>
        <View>
          <Text selectable style={[typography.caption, { color: colors.textMuted }]}>
            Olá, {firstName}
          </Text>
          <Text selectable style={[typography.bodyStrong, { color: colors.text, marginTop: spacing.xxs / 2 }]}>
            {getTodayLabel()}
          </Text>
        </View>
        <Pressable
          accessibilityRole="button"
          accessibilityLabel="Notificações"
          style={[
            {
              alignItems: 'center',
              backgroundColor: colors.surface,
              borderRadius: radii.md,
              height: 38,
              justifyContent: 'center',
              width: 38,
            },
            shadows.card,
          ]}
        >
          <Icon name="bell" size={18} />
        </Pressable>
      </View>

      <View style={{ alignItems: 'center', flexDirection: 'row', gap: spacing.md }}>
        <Mascot size={66} />
        <View style={{ flex: 1 }}>
          <Text selectable style={[typography.title, { color: colors.text }]}>
            Como está a criança hoje?
          </Text>
          <Text selectable style={[typography.body, { color: colors.textMuted, marginTop: spacing.xxs }]}>
            Vamos orientar a triagem inicial em poucos minutos.
          </Text>
        </View>
      </View>

      <View>
        <SectionTitle title="Avaliar para" />
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: spacing.sm }}>
          {childrenList.map((child) => {
            const selected = selectedChild?.id === child.id;
            return (
              <Pressable
                accessibilityRole="button"
                key={child.id}
                onPress={() => onSelectChild(child)}
                style={[
                  {
                    alignItems: 'center',
                    backgroundColor: selected ? colors.navy : colors.surface,
                    borderRadius: radii.xxl,
                    flexDirection: 'row',
                    gap: spacing.sm,
                    minWidth: 150,
                    paddingHorizontal: spacing.sm,
                    paddingVertical: spacing.sm,
                  },
                  shadows.card,
                ]}
              >
                <View
                  style={{
                    alignItems: 'center',
                    backgroundColor: getTint(child.tint),
                    borderRadius: radii.md,
                    height: 38,
                    justifyContent: 'center',
                    width: 38,
                  }}
                >
                  <Text selectable={false} style={[typography.caption, { color: colors.navy }]}>
                    {child.initials}
                  </Text>
                </View>
                <View>
                  <Text selectable style={[typography.bodyStrong, { color: selected ? colors.inverseText : colors.text }]}>
                    {child.name}
                  </Text>
                  <Text selectable style={[typography.caption, { color: selected ? colors.inverseText : colors.textMuted }]}>
                    {child.age}
                  </Text>
                </View>
              </Pressable>
            );
          })}
          <Pressable
            accessibilityRole="button"
            onPress={() => onGo('child-add')}
            style={{
              alignItems: 'center',
              borderColor: colors.hairline,
              borderRadius: radii.xxl,
              borderStyle: 'dashed',
              borderWidth: 1.5,
              gap: spacing.xxs,
              justifyContent: 'center',
              minWidth: 104,
              paddingHorizontal: spacing.md,
            }}
          >
            <Icon name="plus" color={colors.primary} size={20} />
            <Text selectable={false} style={[typography.caption, { color: colors.primary }]}>
              Adicionar
            </Text>
          </Pressable>
        </ScrollView>
      </View>

      <Pressable accessibilityRole="button" onPress={() => onGo('symptoms')}>
        <LinearGradient
          colors={[colors.primary, colors.navy]}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 1 }}
          style={{
            alignItems: 'center',
            borderRadius: radii.hero,
            flexDirection: 'row',
            gap: spacing.md,
            overflow: 'hidden',
            padding: spacing.lg,
          }}
        >
          <View
            style={{
              alignItems: 'center',
              backgroundColor: colors.inverseOverlay,
              borderRadius: radii.xl,
              height: 52,
              justifyContent: 'center',
              width: 52,
            }}
          >
            <Icon name="stetho" color={colors.inverseText} size={26} />
          </View>
          <View style={{ flex: 1 }}>
            <Text selectable={false} style={[typography.subtitle, { color: colors.inverseText }]}>
              Avaliar sintomas
            </Text>
            <Text selectable={false} style={[typography.caption, { color: colors.inverseText, marginTop: spacing.xxs / 2 }]}>
              Triagem guiada em cerca de 2 minutos
            </Text>
          </View>
          <Icon name="chevronRight" color={colors.inverseText} size={20} />
        </LinearGradient>
      </Pressable>

      <View style={{ flexDirection: 'row', gap: spacing.sm }}>
        <Card
          onPress={() => onGo('orientations')}
          style={{ flex: 1 }}
          contentStyle={{ gap: spacing.sm }}
        >
          <Icon name="book" color={colors.lowSolid} size={24} />
          <View>
            <Text selectable style={[typography.bodyStrong, { color: colors.text }]}>
              Orientações
            </Text>
            <Text selectable style={[typography.caption, { color: colors.textMuted }]}>
              Cuidados em casa
            </Text>
          </View>
        </Card>
        <Card onPress={() => onGo('history')} style={{ flex: 1 }} contentStyle={{ gap: spacing.sm }}>
          <Icon name="history" color={colors.primary} size={24} />
          <View>
            <Text selectable style={[typography.bodyStrong, { color: colors.text }]}>
              Histórico
            </Text>
            <Text selectable style={[typography.caption, { color: colors.textMuted }]}>
              {historyItems.length} avaliações
            </Text>
          </View>
        </Card>
      </View>

      <DisclaimerCard />

      {historyItems.length ? (
        <View style={{ gap: spacing.xs }}>
          <SectionTitle
            title="Avaliações recentes"
            action={
              <Pressable onPress={() => onGo('history')}>
                <Text selectable={false} style={[typography.caption, { color: colors.primary }]}>
                  Ver todas
                </Text>
              </Pressable>
            }
          />
          {historyItems.slice(0, 2).map((item) => (
            <HistoryRow item={item} key={item.id} />
          ))}
        </View>
      ) : null}
    </ScrollView>
  );
}
