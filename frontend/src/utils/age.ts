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
    if (ageInMonths < 12 || ageInMonths % 12 !== 0) return { age: formatted, ageUnit: 'meses', ageValue: String(ageInMonths) };
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
  const max = ageUnit === 'meses' ? 155 : 12;
  if (!Number.isFinite(amount) || amount < 0 || amount > max) {
    throw new Error('A criança deve ter até 12 anos.');
  }

  if (amount === 0 && ageUnit === 'anos') throw new Error('Para menores de 1 ano, informe a idade em meses (0 para recém-nascido).');
  return amount;
}

export function validateBirthDate(value: string, today = new Date()): string {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
  if (!match) throw new Error('Informe o nascimento no formato AAAA-MM-DD.');
  const year = Number(match[1]); const month = Number(match[2]); const day = Number(match[3]);
  const date = new Date(year, month - 1, day);
  if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day) throw new Error('Data de nascimento inválida.');
  const now = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  const oldest = new Date(today.getFullYear() - 13, today.getMonth(), today.getDate());
  if (date > now || date <= oldest) throw new Error('O nascimento deve corresponder a uma criança de 0 a 12 anos.');
  return value;
}

export function birthDateAge(value: string, today = new Date()): ParsedAge {
  const [year,month,day] = value.split('-').map(Number);
  const months = (today.getFullYear() - year!) * 12 + today.getMonth() - (month! - 1) - (today.getDate() < day! ? 1 : 0);
  return parseAge(undefined, months);
}
