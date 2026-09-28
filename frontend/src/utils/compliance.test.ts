import { buildTriageShareMessage } from './frontendGaps';
it('never makes emergency care conditional on persistence', () => {
  const message = buildTriageShareMessage({risk:'high', score:9, hasRedFlag:true}, 'Alto risco');
  expect(message).toContain('imediatamente');
  expect(message).not.toContain('se os sinais persistirem');
});
