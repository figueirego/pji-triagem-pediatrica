import { Linking, ScrollView, Text, View } from 'react-native';
import { Card, GhostButton, Icon, ScreenHeader } from '../components';
import { colors, radii, riskPalette, spacing, typography } from '../theme';
import type { OrientationCardItem, RiskTone } from '../types/domain';
import { buildOrientationDetailItems } from '../utils/frontendGaps';

interface OrientationDetailScreenProps {
  item: OrientationCardItem;
  onBack: () => void;
}

function toneStyle(tone: RiskTone) {
  if (tone === 'low') return riskPalette.low;
  if (tone === 'mod') return riskPalette.mod;
  if (tone === 'high') return riskPalette.high;
  return tone === 'primary'
    ? { solid: colors.primary, softer: colors.primarySoft }
    : { solid: colors.textMuted, softer: colors.neutralSoft };
}

export function OrientationDetailScreen({ item, onBack }: OrientationDetailScreenProps) {
  const tone = toneStyle(item.tone);
  const detailItems = buildOrientationDetailItems(item);

  return (
    <ScrollView contentContainerStyle={{ gap: spacing.md, paddingBottom: spacing.tabContentBottom }} contentInsetAdjustmentBehavior="automatic">
      <ScreenHeader title="Detalhe da orientação" onBack={onBack} />
      <View style={{ gap: spacing.md, paddingHorizontal: spacing.lg }}>
        <Card
          style={{ backgroundColor: tone.softer, borderColor: tone.softer }}
          contentStyle={{ alignItems: 'center', gap: spacing.sm }}
        >
          <View
            style={{
              alignItems: 'center',
              backgroundColor: colors.surface,
              borderRadius: radii.avatar,
              height: 72,
              justifyContent: 'center',
              width: 72,
            }}
          >
            <Icon name={item.icon} color={tone.solid} size={34} strokeWidth={2.4} />
          </View>
          <Text selectable style={[typography.title, { color: colors.text, textAlign: 'center' }]}>
            {item.title}
          </Text>
          <Text selectable style={[typography.body, { color: colors.textMuted, textAlign: 'center' }]}>
            {item.subtitle}
          </Text>
        </Card>

        <View style={{ gap: spacing.xs }}>
          <Text selectable style={[typography.eyebrow, { color: colors.textMuted }]}>
            Próximos passos
          </Text>
          {detailItems.map((detail, index) => (
            <Card key={detail} padding={spacing.sm} contentStyle={{ flexDirection: 'row', gap: spacing.sm }}>
              <View
                style={{
                  alignItems: 'center',
                  backgroundColor: tone.softer,
                  borderRadius: radii.sm,
                  height: 30,
                  justifyContent: 'center',
                  width: 30,
                }}
              >
                <Text selectable={false} style={[typography.caption, { color: tone.solid, fontVariant: ['tabular-nums'] }]}>
                  {index + 1}
                </Text>
              </View>
              <Text selectable style={[typography.bodyStrong, { color: colors.text, flex: 1 }]}>
                {detail}
              </Text>
            </Card>
          ))}
        </View>

        {item.tone === 'high' ? (
          <GhostButton
            onPress={() => {
              void Linking.openURL('tel:192');
            }}
            icon={<Icon name="phone" size={18} color={colors.highSolid} />}
          >
            Ligar para SAMU 192
          </GhostButton>
        ) : null}
      </View>
    </ScrollView>
  );
}
