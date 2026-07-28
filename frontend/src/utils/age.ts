import type { AgeUnit } from '../types/domain';

export interface ParsedAge {
  age: string;
  ageUnit?: AgeUnit;
  ageValue?: string;
}

export function formatAge(ageInMonths: number): string {
  if (!Number.isFinite(ageInMonths) || ageInMonths < 0) {
    return 'Idade não informada';
  }

  if (ageInMonths < 12) {
    return `${ageInMonths} ${ageInMonths === 1 ? 'mês' : 'meses'}`;
  }

  const years = Math.floor(ageInMonths / 12);
  const months = ageInMonths % 12;

  if (months === 0) {
    return `${years} ${years === 1 ? 'ano' : 'anos'}`;
  }

  return `${years} ${years === 1 ? 'ano' : 'anos'} e ${months} ${months === 1 ? 'mês' : 'meses'}`;
}

export function parseAge(age?: string, ageInMonths?: number): ParsedAge {
  if (typeof ageInMonths === 'number' && ageInMonths >= 0) {
    const formatted = formatAge(ageInMonths);
    if (ageInMonths < 12) return { age: formatted, ageUnit: 'meses', ageValue: String(ageInMonths) };
    return { age: formatted, ageUnit: 'anos', ageValue: String(Math.floor(ageInMonths / 12)) };
  }

  const label = age || 'Idade não informada';
  const match = label.match(/(\d+)\s*(m[eê]s|meses|ano|anos)/i);
  if (!match) return { age: label };

  return {
    age: label,
    ageUnit: match[2]?.toLowerCase().startsWith('m') ? 'meses' : 'anos',
    ageValue: match[1],
  };
}

export function parseAgeAmount(ageValue: string | undefined, ageUnit: AgeUnit = 'anos'): number {
  const trimmed = String(ageValue || '').trim();
  if (!/^\d+$/.test(trimmed)) {
    throw new Error('Informe uma idade válida.');
  }

  const amount = Number.parseInt(trimmed, 10);
  const max = ageUnit === 'meses' ? 156 : 12;
  if (!Number.isFinite(amount) || amount < 0 || amount > max) {
    throw new Error('A criança deve ter até 12 anos.');
  }

  return amount;
}
