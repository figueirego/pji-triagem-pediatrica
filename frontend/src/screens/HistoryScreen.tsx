import { useMemo, useState } from 'react';
import { Pressable, RefreshControl, ScrollView, Text, View } from 'react-native';
import { Card, EmptyState, HistoryRow, ScreenHeader } from '../components';
import { colors, radii, riskPalette, shadows, spacing, typography } from '../theme';
import type { ChildProfile, HistoryItem } from '../types/domain';

interface StatProps {
  label: string;
  value: number;
  tone: string;
}

interface HistoryScreenProps {
  childrenList: ChildProfile[];
  historyItems?: HistoryItem[];
  isRefreshing?: boolean;
  onBack: () => void;
  onRefresh?: () => Promise<void> | void;
}

interface HistoryFilterTab {
  id: string;
  label: string;
}

function Stat({ label, value, tone }: StatProps) {
  return (
    <Card padding={spacing.sm} style={{ flex: 1 }} contentStyle={{ alignItems: 'center' }}>
      <Text selectable style={[typography.title, { color: tone, fontVariant: ['tabular-nums'] }]}>
        {value}
      </Text>
      <Text selectable style={[typography.caption, { color: colors.textMuted, textAlign: 'center' }]}>
        {label}
      </Text>
    </Card>
  );
}

export function HistoryScreen({ childrenList, historyItems = [], isRefreshing = false, onBack, onRefresh }: HistoryScreenProps) {
  const [filter, setFilter] = useState('all');
  const [localRefreshing, setLocalRefreshing] = useState(false);
  const tabs = useMemo<HistoryFilterTab[]>(
    () => [{ id: 'all', label: 'Todos' }, ...childrenList.map((child) => ({ id: child.name, label: child.name }))],
    [childrenList],
  );
  const filtered = filter === 'all' ? historyItems : historyItems.filter((item) => item.child === filter);

  async function handleRefresh() {
    if (!onRefresh) return;
    setLocalRefreshing(true);
    try {
      await onRefresh();
    } finally {
      setLocalRefreshing(false);
    }
  }

  return (
    <ScrollView
      contentContainerStyle={{ gap: spacing.md, paddingBottom: spacing.tabContentBottom }}
      contentInsetAdjustmentBehavior="automatic"
      refreshControl={onRefresh ? <RefreshControl refreshing={isRefreshing || localRefreshing} onRefresh={handleRefresh} /> : undefined}
    >
      <ScreenHeader title="Histórico" onBack={onBack} />
      <View style={{ gap: spacing.md, paddingHorizontal: spacing.lg }}>
        <View style={{ flexDirection: 'row', gap: spacing.xs }}>
          <Stat label="Avaliações" value={historyItems.length} tone={colors.primary} />
          <Stat label="Baixo risco" value={historyItems.filter((item) => item.risk === 'low').length} tone={riskPalette.low.solid} />
          <Stat label="Alto risco" value={historyItems.filter((item) => item.risk === 'high').length} tone={riskPalette.high.solid} />
        </View>

        <View
          style={[
            {
              backgroundColor: colors.surface,
              borderRadius: radii.lg,
              flexDirection: 'row',
              gap: spacing.xxs,
              padding: spacing.xxs,
            },
            shadows.card,
          ]}
        >
          {tabs.map((tab) => {
            const selected = filter === tab.id;
            return (
              <Pressable
                accessibilityRole="tab"
                accessibilityState={{ selected }}
                key={tab.id}
                onPress={() => setFilter(tab.id)}
                style={{
                  alignItems: 'center',
                  backgroundColor: selected ? colors.primary : 'transparent',
                  borderRadius: radii.sm,
                  flex: 1,
                  paddingHorizontal: spacing.xs,
                  paddingVertical: spacing.xs,
                }}
              >
                <Text selectable={false} numberOfLines={1} adjustsFontSizeToFit style={[typography.caption, { color: selected ? colors.inverseText : colors.textMuted }]}>
                  {tab.label}
                </Text>
              </Pressable>
            );
          })}
        </View>

        <View style={{ gap: spacing.xs }}>
          {filtered.length ? (
            filtered.map((item) => <HistoryRow key={item.id} item={item} />)
          ) : (
            <EmptyState title="Nenhuma avaliação ainda" message="Faça a primeira!" />
          )}
        </View>
      </View>
    </ScrollView>
  );
}
