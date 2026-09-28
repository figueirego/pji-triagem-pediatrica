import { validateBirthDate } from './age';

/** UI-only mask; API and domain dates remain ISO calendar strings. */
export function maskBirthDateInput(value: string): string {
  return value.replace(/\D/g, '').slice(0, 8)
    .replace(/^(\d{2})(\d)/, '$1/$2')
    .replace(/^(\d{2})\/(\d{2})(\d)/, '$1/$2/$3');
}

export function formatBirthDateInput(isoDate?: string): string {
  if (!isoDate) return '';
  return isoDate.replace(/^(\d{4})-(\d{2})-(\d{2})$/, '$3/$2/$1');
}

export function parseBirthDateInput(value: string, today = new Date()): string {
  const match = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(value);
  if (!match) throw new Error('Informe o nascimento no formato DD/MM/AAAA.');
  return validateBirthDate(`${match[3]}-${match[2]}-${match[1]}`, today);
}
