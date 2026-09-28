import { parseAgeAmount, parseAge } from './age';
describe('pediatric age boundaries', () => {
  it('accepts newborn months and twelve years', () => {
    expect(parseAgeAmount('0', 'meses')).toBe(0);
    expect(parseAgeAmount('12', 'anos')).toBe(12);
    expect(parseAgeAmount('155', 'meses')).toBe(155);
  });
  it('rejects thirteen years and ambiguous zero years', () => {
    expect(() => parseAgeAmount('156', 'meses')).toThrow();
    expect(() => parseAgeAmount('0', 'anos')).toThrow();
  });
  it('preserves month precision on edit', () => {
    expect(parseAge(undefined, 17)).toMatchObject({ ageUnit: 'meses', ageValue: '17' });
  });
});

import { validateBirthDate } from './age';
it('validates actual calendar dates and pediatric date range',()=>{
 expect(validateBirthDate('2026-09-01',new Date(2026,8,24))).toBe('2026-09-01');
 expect(()=>validateBirthDate('2026-02-31',new Date(2026,8,24))).toThrow();
 expect(()=>validateBirthDate('2026-09-25',new Date(2026,8,24))).toThrow();
 expect(()=>validateBirthDate('2013-09-24',new Date(2026,8,24))).toThrow();
});
