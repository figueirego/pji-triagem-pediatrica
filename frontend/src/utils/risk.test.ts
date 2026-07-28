import { calculateRisk } from './risk';
import type { TriageQuestion } from '../types/domain';

describe('calculateRisk', () => {
  it('treats a null question list from runtime data as an empty questionnaire', () => {
    expect(calculateRisk(null as unknown as TriageQuestion[], {})).toEqual({
      hasRedFlag: false,
      risk: 'low',
      score: 0,
    });
  });
});
