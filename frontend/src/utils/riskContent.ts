import type { RiskContentMap } from '../types/domain';

export const riskContent: RiskContentMap = {
  low: {
    title: 'Observação domiciliar',
    message: 'No momento, os sinais informados parecem leves. Acompanhe a evolução e siga as orientações.',
    cta: 'Ver cuidados em casa',
    icon: 'check',
    mood: 'calm',
    actions: ['Observe a evolução dos sintomas.', 'Procure avaliação se houver piora, novos sinais de alerta ou dúvida.'],
  },
  mod: {
    title: 'Avaliação médica nas próximas horas',
    message: 'Procure pediatra ou pronto atendimento nas próximas horas, em até 24 horas. Antecipe se houver piora.',
    cta: 'Ver cuidados até a consulta',
    icon: 'warn',
    mood: 'watch',
    actions: ['Procure pediatra ou pronto atendimento nas próximas horas, em até 24 horas.', 'Antecipe a avaliação se houver piora ou novos sinais de alerta.'],
  },
  high: {
    title: 'Atendimento imediato',
    message: 'Os sinais informados indicam necessidade de atendimento médico urgente.',
    cta: 'Ver orientações imediatas',
    icon: 'alert',
    mood: 'alert',
    actions: ['Procure um serviço de emergência imediatamente.', 'Em uma emergência, ligue para o SAMU 192. Não aguarde melhora para procurar ajuda.'],
  },
};
