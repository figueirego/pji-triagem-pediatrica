import { educationalSymptomCodes, symptomEducation } from './education';
import {buildOrientationDetailItems} from './frontendGaps';
it('provides distinct guidance and reference for every required symptom',()=>{
 expect(educationalSymptomCodes).toHaveLength(9);
 const details = educationalSymptomCodes.map(code=>{
  const item=symptomEducation({id:code,code,name:code,desc:'',tone:'primary',icon:'activity'});
  expect(item.sourceUrl).toMatch(/^https:\/\/www.nhs.uk\//);
  expect(item.details?.length).toBeGreaterThan(1);
  expect(buildOrientationDetailItems(item)).toEqual(item.details);
  return item.details?.join();
 });
 expect(new Set(details).size).toBe(9);
});
it('preserves actual server orientation description',()=>{
 expect(buildOrientationDetailItems({id:'home-3',title:'Hidratação',subtitle:'Conteúdo salvo para esta avaliação',tone:'low',icon:'book'})).toContain('Conteúdo salvo para esta avaliação');
});
