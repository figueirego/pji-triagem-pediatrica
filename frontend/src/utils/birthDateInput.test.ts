import { maskBirthDateInput, formatBirthDateInput, parseBirthDateInput } from './birthDateInput';
const today = new Date(2026, 8, 25);
describe('birth date field mask and calendar validation', () => {
  it('formats partial typing and deletion with automatic separators', () => {
    expect(['', '0', '01', '010', '0109', '01092', '01092026'].map(maskBirthDateInput)).toEqual(['', '0', '01', '01/0', '01/09', '01/09/2', '01/09/2026']);
    expect(maskBirthDateInput('01/09/202')).toBe('01/09/202');
  });
  it('sanitizes pasted characters and limits to eight digits', () => {
    expect(maskBirthDateInput('01x09y2026!')).toBe('01/09/2026');
    expect(maskBirthDateInput('01092026999')).toBe('01/09/2026');
    expect(maskBirthDateInput('01/09/2026')).toBe('01/09/2026');
  });
  it('preserves ISO dates through localized editing without timezone conversion', () => {
    expect(formatBirthDateInput('2024-02-29')).toBe('29/02/2024');
    expect(parseBirthDateInput(formatBirthDateInput('2024-02-29'), today)).toBe('2024-02-29');
    expect(formatBirthDateInput()).toBe('');
  });
  it.each(['1/09/2026', '01/9/2026', '2026-09-01', '01/09/26', '31/02/2026', '29/02/2025', '00/09/2026', '01/13/2026', '26/09/2026', '25/09/2013'])('rejects malformed, impossible or out-of-range date %s', value => {
    expect(() => parseBirthDateInput(value, today)).toThrow();
  });
  it('accepts valid leap day, newborn date and last day before thirteen', () => {
    expect(parseBirthDateInput('29/02/2024', today)).toBe('2024-02-29');
    expect(parseBirthDateInput('25/09/2026', today)).toBe('2026-09-25');
    expect(parseBirthDateInput('26/09/2013', today)).toBe('2013-09-26');
  });
});
