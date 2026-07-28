import type { RiskLevel, TriageAnswers, TriageQuestion, TriageResult, YesNoAnswer } from '../types/domain';

export function calculateRisk(questions: TriageQuestion[] | null | undefined, answers: TriageAnswers): TriageResult {
  let score = 0;
  let hasRedFlag = false;
  const safeQuestions = Array.isArray(questions) ? questions : [];

  safeQuestions.forEach((question, index) => {
    const answer = answers[index];
    if (!answer) return;

    if (question.type === 'options') {
      const option = question.options.find((item) => item.id === answer);
      score += option?.risk ?? 0;
      return;
    }

    const answerScore = question.weights[answer as YesNoAnswer] || 0;
    score += answerScore;

    if (question.redFlag && answer === 'yes') {
      hasRedFlag = true;
    }
  });

  if (hasRedFlag || score >= 6) return { risk: 'high', score, hasRedFlag };
  if (score >= 3) return { risk: 'mod', score, hasRedFlag };
  return { risk: 'low', score, hasRedFlag };
}

export function formatRiskLabel(risk: RiskLevel): string {
  if (risk === 'high') return 'Alto risco';
  if (risk === 'mod') return 'Risco moderado';
  return 'Baixo risco';
}
