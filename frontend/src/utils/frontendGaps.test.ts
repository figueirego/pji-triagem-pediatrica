import type { OrientationCardItem, Symptom, TriageResult } from '../types/domain';
import { buildOrientationDetailItems, buildTriageShareMessage, filterSymptoms } from './frontendGaps';

const symptoms: Symptom[] = [
  {
    desc: 'Temperatura acima de 37,8°C',
    icon: 'thermo',
    id: 'fever',
    name: 'Febre',
    tone: 'mod',
  },
  {
    desc: 'Respiração rápida ou difícil',
    icon: 'lung',
    id: 'breath',
    name: 'Falta de ar',
    tone: 'high',
  },
];

describe('frontend gap helpers', () => {
  it('filters symptoms by normalized name and description', () => {
    expect(filterSymptoms(symptoms, 'respiracao')).toEqual([symptoms[1]]);
    expect(filterSymptoms(symptoms, 'FEBRE')).toEqual([symptoms[0]]);
    expect(filterSymptoms(symptoms, '  ')).toEqual(symptoms);
  });

  it('builds a shareable triage summary without exposing internal ids', () => {
    const result: TriageResult = {
      child: {
        age: '4 anos',
        id: 'child-1',
        initials: 'MA',
        name: 'Maria',
        tint: 'primary',
        weight: '17 kg',
      },
      hasRedFlag: true,
      risk: 'high',
      score: 9,
      symptom: symptoms[1],
    };

    const message = buildTriageShareMessage(result, 'Alto risco');

    expect(message).toContain('Maria');
    expect(message).toContain('Falta de ar');
    expect(message).toContain('Alto risco');
    expect(message).toContain('Procure atendimento de urgência');
    expect(message).not.toContain('child-1');
  });

  it('returns concrete detail items for an emergency orientation card', () => {
    const orientation: OrientationCardItem = {
      icon: 'phone',
      id: 'help',
      subtitle: 'Sinais para ir à emergência sem demora',
      title: 'Quando procurar ajuda',
      tone: 'high',
    };

    expect(buildOrientationDetailItems(orientation)).toEqual(
      expect.arrayContaining([
        'Acione o SAMU 192 ou vá ao serviço de emergência se houver piora rápida.',
        'Mantenha a criança confortável e evite oferecer alimentos se houver sonolência intensa ou falta de ar.',
      ]),
    );
  });
});
