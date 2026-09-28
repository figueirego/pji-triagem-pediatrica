import type { OrientationCardItem, Symptom } from '../types/domain';
const urgentSource = 'https://www.nhs.uk/baby/health/when-to-get-urgent-medical-help-for-babies-and-children-under-5/';
const digestiveSource = 'https://www.nhs.uk/symptoms/diarrhoea-and-vomiting/';
const entries: Record<string, {details:string[]; sourceUrl:string}> = {
 FEBRE: {details:['Registre a temperatura medida e o horário. A idade do bebê muda a urgência da avaliação.', 'Bebê menor de 3 meses com febre precisa de atendimento imediato. Não espere terminar o questionário.'],sourceUrl:urgentSource},
 TOSSE: {details:['Observe se a tosse vem acompanhada de esforço para respirar ou dificuldade para beber.', 'Respiração muito rápida, retração do tórax ou lábios arroxeados exigem atendimento imediato.'],sourceUrl:urgentSource},
 VOMITOS: {details:['Mantenha a amamentação; ofereça mamadas menores e mais frequentes se necessário.', 'Observe se consegue manter líquidos. Vômito com sangue, sonolência importante ou incapacidade de beber exigem avaliação urgente.'],sourceUrl:digestiveSource},
 DIARREIA: {details:['Ofereça líquidos com frequência, mantenha a amamentação e observe a urina.', 'Evite refrigerantes e sucos. Não dê medicamentos para interromper a diarreia sem orientação profissional.'],sourceUrl:digestiveSource},
 DOR_ABDOMINAL: {details:['Registre onde dói, quando começou e se há vômitos ou alteração nas fezes.', 'Dor abdominal forte ou persistente, barriga inchada ou piora do estado geral exigem avaliação médica.'],sourceUrl:digestiveSource},
 FALTA_DE_AR: {details:['Observe esforço para respirar, pausas e dificuldade para falar ou mamar.', 'Se há dificuldade para respirar agora, procure emergência imediatamente; em situação grave, ligue 192.'],sourceUrl:urgentSource},
 MANCHAS_PELE: {details:['Observe cor, extensão e início das manchas.', 'Manchas que não desaparecem à pressão ou associadas a piora rápida exigem emergência. Não aguarde novas manchas.'],sourceUrl:urgentSource},
 TRAUMA_LEVE: {details:['Anote como ocorreu a queda ou batida e quais partes foram atingidas.', 'Perda de consciência, dificuldade para despertar ou não conseguir usar um membro exigem avaliação imediata.'],sourceUrl:urgentSource},
 DOR_OUVIDO: {details:['Mantenha o ouvido seco. Não introduza cotonetes, objetos ou gotas sem orientação.', 'Procure avaliação se houver secreção, inchaço ao redor do ouvido, alteração da audição ou piora do estado geral.'],sourceUrl:'https://www.nhs.uk/conditions/ear-infections/'},
};
export function symptomEducation(symptom:Symptom): OrientationCardItem {
 const entry = entries[symptom.code || ''];
 return {id:`symptom-${symptom.id}`,title:symptom.name,subtitle:'Orientações educativas para pais e cuidadores',icon:symptom.icon,tone:symptom.tone,...entry};
}
export const educationalSymptomCodes = Object.keys(entries);
