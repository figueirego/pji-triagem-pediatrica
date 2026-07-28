import { useState } from 'react';
import { Alert, Pressable, ScrollView, Text, View } from 'react-native';
import { Card, GhostButton, Icon, PrimaryButton, ScreenHeader, TextField } from '../components';
import { colors, radii, riskPalette, shadows, spacing, typography } from '../theme';
import { parseAgeAmount } from '../utils/age';
import type { AgeUnit, ChildProfile, RiskTone } from '../types/domain';

type AvatarTint = Extract<RiskTone, 'primary' | 'low' | 'mod' | 'high'>;

const avatarOptions = [
  { emoji: '🌸', tint: colors.primarySoft },
  { emoji: '🌱', tint: riskPalette.low.softer },
  { emoji: '⭐', tint: riskPalette.mod.softer },
  { emoji: '🐻', tint: colors.neutralSoft },
  { emoji: '🦊', tint: riskPalette.high.softer },
  { emoji: '🌈', tint: colors.primarySoft },
];

interface ChildFormScreenProps {
  initialChild?: ChildProfile | null;
  onBack: () => void;
  onDelete?: (child: ChildProfile) => Promise<void> | void;
  onSave: (child: ChildProfile) => Promise<void> | void;
}

function avatarTint(value?: RiskTone): AvatarTint {
  return value === 'low' || value === 'mod' || value === 'high' ? value : 'primary';
}

function weightValue(weight?: string): string {
  const match = (weight || '').replace(',', '.').match(/\d+(\.\d+)?/);
  return match?.[0] || '';
}

export function ChildFormScreen({ initialChild, onBack, onDelete, onSave }: ChildFormScreenProps) {
  const isEditing = Boolean(initialChild);
  const [name, setName] = useState(initialChild?.name || '');
  const [ageValue, setAgeValue] = useState(initialChild?.ageValue || '');
  const [ageUnit, setAgeUnit] = useState<AgeUnit>(initialChild?.ageUnit || 'anos');
  const [weight, setWeight] = useState(weightValue(initialChild?.weight));
  const [tint, setTint] = useState<AvatarTint>(avatarTint(initialChild?.tint));
  const [avatarEmoji, setAvatarEmoji] = useState(initialChild?.avatarEmoji || '🌸');
  const [error, setError] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  async function handleSave() {
    setError(null);
    const trimmedName = name.trim();
    if (!trimmedName || !ageValue.trim()) {
      setError('Informe nome e idade da criança.');
      return;
    }
    let parsedAgeValue: number;
    try {
      parsedAgeValue = parseAgeAmount(ageValue, ageUnit);
    } catch (ageError) {
      setError(ageError instanceof Error ? ageError.message : 'Informe uma idade válida.');
      return;
    }

    const initials = trimmedName
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part.charAt(0))
      .join('')
      .toUpperCase();

    const child: ChildProfile = {
      id: initialChild?.id || `${trimmedName.toLowerCase().replace(/\s+/g, '-')}-${Date.now()}`,
      backendId: initialChild?.backendId,
      name: trimmedName,
      ageValue: String(parsedAgeValue),
      ageUnit,
      age: `${parsedAgeValue} ${ageUnit}`,
      weight: weight ? `${weight} kg` : 'Peso não informado',
      initials,
      tint,
      avatarEmoji,
    };

    setIsSaving(true);
    try {
      await onSave(child);
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : 'Não foi possível salvar a criança.');
    } finally {
      setIsSaving(false);
    }
  }

  function handleDelete() {
    if (!initialChild || !onDelete) return;

    Alert.alert('Excluir criança', `Remover ${initialChild.name} do perfil?`, [
      { text: 'Cancelar', style: 'cancel' },
      {
        onPress: async () => {
          setError(null);
          setIsDeleting(true);
          try {
            await onDelete(initialChild);
          } catch (deleteError) {
            setError(deleteError instanceof Error ? deleteError.message : 'Não foi possível excluir a criança.');
          } finally {
            setIsDeleting(false);
          }
        },
        style: 'destructive',
        text: 'Excluir',
      },
    ]);
  }

  return (
    <ScrollView contentContainerStyle={{ gap: spacing.md, paddingBottom: spacing.xxl }} contentInsetAdjustmentBehavior="automatic">
      <ScreenHeader title={isEditing ? 'Editar criança' : 'Cadastrar criança'} onBack={onBack} />
      <View style={{ alignItems: 'center', gap: spacing.sm }}>
        <View
          style={[
            {
              alignItems: 'center',
              backgroundColor: avatarOptions.find((item) => item.emoji === avatarEmoji)?.tint || colors.primarySoft,
              borderRadius: radii.avatar,
              height: 80,
              justifyContent: 'center',
              width: 80,
            },
            shadows.card,
          ]}
        >
          <Text selectable={false} style={[typography.title, { color: colors.navy }]}>
            {avatarEmoji}
          </Text>
        </View>
        <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: spacing.xs, justifyContent: 'center', maxWidth: 260 }}>
          {avatarOptions.map((option) => (
            <Pressable
              accessibilityRole="button"
              accessibilityLabel={`Avatar ${option.emoji}`}
              key={option.emoji}
              onPress={() => {
                setAvatarEmoji(option.emoji);
                setTint(option.emoji === '🌱' ? 'low' : option.emoji === '⭐' ? 'mod' : option.emoji === '🦊' ? 'high' : 'primary');
              }}
              style={[
                {
                  alignItems: 'center',
                  backgroundColor: avatarEmoji === option.emoji ? colors.navy : colors.surface,
                  borderRadius: radii.md,
                  height: 34,
                  justifyContent: 'center',
                  width: 34,
                },
                shadows.card,
              ]}
            >
              <Text selectable={false} style={typography.caption}>
                {option.emoji}
              </Text>
            </Pressable>
          ))}
        </View>
      </View>

      <Card style={{ marginHorizontal: spacing.lg }} contentStyle={{ gap: spacing.md }}>
        <TextField label="Nome da criança" onChangeText={setName} placeholder="Ex.: Maria" value={name} />
        <View style={{ gap: spacing.xs }}>
          <TextField
            inputMode="numeric"
            label="Idade"
            onChangeText={setAgeValue}
            placeholder="Ex.: 4"
            value={ageValue}
          />
          <View
            style={[
              {
                alignSelf: 'flex-start',
                backgroundColor: colors.surface,
                borderRadius: radii.lg,
                flexDirection: 'row',
                gap: spacing.xxs,
                padding: spacing.xxs,
              },
              shadows.card,
            ]}
          >
            {(['meses', 'anos'] satisfies AgeUnit[]).map((unit) => {
              const selected = ageUnit === unit;
              return (
                <Pressable
                  accessibilityRole="button"
                  key={unit}
                  onPress={() => setAgeUnit(unit)}
                  style={{
                    backgroundColor: selected ? colors.primary : 'transparent',
                    borderRadius: radii.sm,
                    paddingHorizontal: spacing.sm,
                    paddingVertical: spacing.xs,
                  }}
                >
                  <Text selectable={false} style={[typography.caption, { color: selected ? colors.inverseText : colors.text }]}>
                    {unit}
                  </Text>
                </Pressable>
              );
            })}
          </View>
        </View>
        <TextField
          hint="Ajuda em cálculos futuros de dose e hidratação."
          inputMode="decimal"
          label="Peso (opcional)"
          onChangeText={setWeight}
          placeholder="Ex.: 17"
          value={weight}
        />
        {error ? (
          <Text selectable style={[typography.caption, { color: colors.dangerText }]}>
            {error}
          </Text>
        ) : null}
        <PrimaryButton
          disabled={isDeleting}
          loading={isSaving}
          onPress={handleSave}
          icon={<Icon name="check" color={colors.inverseText} size={18} />}
        >
          {isEditing ? 'Salvar alterações' : 'Salvar criança'}
        </PrimaryButton>
        {isEditing && onDelete ? (
          <GhostButton
            disabled={isSaving || isDeleting}
            onPress={handleDelete}
            icon={<Icon name="close" color={colors.highSolid} size={18} />}
            style={{ borderColor: colors.highSoft }}
          >
            {isDeleting ? 'Excluindo...' : 'Excluir criança'}
          </GhostButton>
        ) : null}
      </Card>
    </ScrollView>
  );
}
