import type { OrientationCardItem, RiskLevel, Symptom, TriageResult } from '../types/domain';

function normalizeText(value: string): string {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .trim()
    .toLowerCase();
}

export function filterSymptoms(symptoms: Symptom[], query: string): Symptom[] {
  const normalizedQuery = normalizeText(query);
  if (!normalizedQuery) return symptoms;

  return symptoms.filter((symptom) => {
    const searchableText = normalizeText(`${symptom.name} ${symptom.desc}`);
    return searchableText.includes(normalizedQuery);
  });
}

function emergencyInstruction(risk: RiskLevel): string {
  if (risk === 'high') return 'Procure atendimento de urgência se os sinais persistirem ou piorarem.';
  if (risk === 'mod') return 'Procure avaliação médica nas próximas horas e monitore a evolução.';
  return 'Observe em casa e procure atendimento se surgirem sinais de alerta.';
}

export function buildTriageShareMessage(result: TriageResult | null, riskLabel: string): string {
  const childName = result?.child?.name || 'Criança';
  const childAge = result?.child?.age ? `, ${result.child.age}` : '';
  const symptom = result?.symptom?.name || 'Sintoma não informado';
  const score = result?.score ?? 0;
  const redFlag = result?.hasRedFlag ? 'Sinal de alerta informado.' : 'Sem sinal de alerta crítico informado.';

  return [
    'Resultado da triagem pediátrica',
    `${childName}${childAge}`,
    `Sintoma principal: ${symptom}`,
    `Classificação: ${riskLabel}`,
    `Pontuação: ${score}`,
    redFlag,
    emergencyInstruction(result?.risk || 'low'),
    'Esta orientação não substitui avaliação médica.',
  ].join('\n');
}

export function buildOrientationDetailItems(item: OrientationCardItem): string[] {
  if (item.tone === 'high' || item.id.startsWith('help')) {
    return [
      'Acione o SAMU 192 ou vá ao serviço de emergência se houver piora rápida.',
      'Mantenha a criança confortável e evite oferecer alimentos se houver sonolência intensa ou falta de ar.',
      'Leve documentos, medicações em uso e informações sobre início dos sintomas.',
    ];
  }

  if (item.tone === 'mod' || item.id.startsWith('warning')) {
    return [
      'Observe febre persistente, respiração diferente, sonolência, vômitos repetidos ou piora do estado geral.',
      'Registre horários, temperatura e mudanças percebidas para informar ao profissional de saúde.',
      'Antecipe a avaliação médica se os sinais ficarem mais intensos.',
    ];
  }

  if (item.tone === 'low' || item.id.startsWith('home')) {
    return [
      'Ofereça líquidos em pequenas quantidades e mantenha repouso em ambiente confortável.',
      'Use apenas medicações já orientadas por profissional de saúde.',
      'Reavalie periodicamente e volte à triagem se surgirem novos sintomas.',
    ];
  }

  return [
    'Acompanhe a evolução dos sintomas ao longo do dia.',
    'Anote mudanças importantes para relatar durante uma consulta.',
    'Procure atendimento se houver piora ou dúvida sobre a segurança da criança.',
  ];
}
