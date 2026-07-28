import { useState } from 'react';
import { KeyboardAvoidingView, Platform, ScrollView, Text } from 'react-native';
import { Card, Icon, PrimaryButton, ScreenHeader, TextField } from '../components';
import { colors, spacing, typography } from '../theme';
import { useAuth } from '../hooks/useAuth';

interface RegisterScreenProps {
  onBack: () => void;
}

function getSubmitErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : 'Não foi possível criar a conta agora.';
}

export function RegisterScreen({ onBack }: RegisterScreenProps) {
  const { isSubmitting, register } = useAuth();
  const [name, setName] = useState('');
  const [document, setDocument] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit() {
    setError(null);
    const normalizedDocument = document.replace(/\D/g, '');
    if (!name.trim() || !normalizedDocument || !email.trim() || !password) {
      setError('Preencha nome, CPF, email e senha.');
      return;
    }
    if (normalizedDocument.length !== 11) {
      setError('Informe um CPF com 11 dígitos.');
      return;
    }
    if (password.length < 6) {
      setError('Use uma senha com pelo menos 6 caracteres.');
      return;
    }
    if (password !== confirm) {
      setError('A confirmação de senha não confere.');
      return;
    }

    try {
      await register({ email, login: normalizedDocument, name, password });
    } catch (submitError) {
      setError(getSubmitErrorMessage(submitError));
    }
  }

  return (
    <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={{ flex: 1 }}>
      <ScrollView
        contentContainerStyle={{ gap: spacing.md, paddingBottom: spacing.xxl }}
        keyboardShouldPersistTaps="handled"
      >
        <ScreenHeader title="Criar conta" onBack={onBack} />
        <Card style={{ marginHorizontal: spacing.lg }} contentStyle={{ gap: spacing.md }}>
          <TextField label="Nome completo" onChangeText={setName} placeholder="Ex.: Camila Ribeiro" value={name} />
          <TextField
            autoCapitalize="none"
            inputMode="numeric"
            label="CPF"
            onChangeText={setDocument}
            placeholder="Somente números"
            value={document}
          />
          <TextField
            autoCapitalize="none"
            autoComplete="email"
            inputMode="email"
            label="Email"
            onChangeText={setEmail}
            placeholder="voce@email.com"
            value={email}
          />
          <TextField label="Senha" onChangeText={setPassword} secureTextEntry value={password} />
          <TextField label="Confirmar senha" onChangeText={setConfirm} secureTextEntry value={confirm} />
          {error ? (
            <Text selectable style={[typography.caption, { color: colors.dangerText }]}>
              {error}
            </Text>
          ) : null}
          <PrimaryButton loading={isSubmitting} onPress={handleSubmit} icon={<Icon name="check" color={colors.inverseText} size={18} />}>
            Salvar cadastro
          </PrimaryButton>
        </Card>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}
