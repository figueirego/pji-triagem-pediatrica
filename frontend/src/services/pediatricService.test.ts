jest.mock('./api',()=>({api:{get:jest.fn(),put:jest.fn(),post:jest.fn()},unwrapData:(value:unknown)=>value}));
import { api } from './api';
import { getChildren, updateChild, getAssessmentResult, getHistory, submitAssessment } from './pediatricService';
it('preserves precise birth date and weight while editing name',async()=>{
 (api.get as jest.Mock).mockResolvedValueOnce({data:[{id:1,name:'Maria',ageInMonths:17,birthDate:'2025-04-07',weightKg:10.25}]});
 const [child] = await getChildren(1);
 (api.put as jest.Mock).mockResolvedValueOnce({data:{id:1,name:'Ana',ageInMonths:17,birthDate:'2025-04-07',weightKg:10.25}});
 if (!child) throw new Error('Missing test child');
 await updateChild(1,{...child,name:'Ana'});
 expect(api.put).toHaveBeenCalledWith('/children/1/children/1',expect.objectContaining({birthDate:'2025-04-07',weightKg:10.25,name:'Ana'}));
});
it('maps history by stable child id',async()=>{
 (api.get as jest.Mock).mockResolvedValueOnce({data:{assessments:[{id:5,childId:2,childName:'Maria',classification:'LOW'}]}});
 expect(await getHistory(1)).toEqual([expect.objectContaining({childId:'2'})]);
});
it('loads saved date and age, and rejects unknown classifications',async()=>{
 const item = {id:'5',childId:'2',child:'Maria',symptom:'Febre',date:'Hoje',risk:'high' as const};
 (api.get as jest.Mock).mockResolvedValueOnce({data:{assessmentId:5,childId:2,finalClassification:'HIGH',createdAt:'2026-01-01T12:00:00',childAgeAtAssessment:'2 meses',childName:'Maria'}});
 const result = await getAssessmentResult(item,[],[]);
 expect(result.child?.age).toBe('2 meses');
 expect(result.answeredAt).toBe('2026-01-01T12:00:00');
 (api.get as jest.Mock).mockResolvedValueOnce({data:{finalClassification:'UNKNOWN'}});
 await expect(getAssessmentResult(item,[],[])).rejects.toThrow('Classificação indisponível');
});
it('preserves historical metadata and uses server snapshot for fresh assessment',async()=>{
 (api.get as jest.Mock).mockResolvedValueOnce({data:{assessments:[{id:8,childId:2,childName:'Nome salvo',classification:'HIGH',childAgeAtAssessment:'2 meses',reason:'Febre neonatal',protocolVersion:'v1'}]}});
 expect(await getHistory(1)).toEqual([expect.objectContaining({childAgeAtAssessment:'2 meses',reason:'Febre neonatal',protocolVersion:'v1'})]);
});

it('uses backend snapshots immediately after submitting',async()=>{
 (api.post as jest.Mock).mockResolvedValueOnce({data:{assessmentId:20,childId:1,finalClassification:'HIGH',childName:'Nome salvo',childAgeAtAssessment:'2 meses'}});
 const result = await submitAssessment({id:'1',name:'Nome atual',age:'3 meses',initials:'N',weight:'',tint:'primary'},{id:'1',name:'Febre',desc:'',icon:'thermo',tone:'mod'},[],{});
 expect(result.child).toMatchObject({name:'Nome salvo',age:'2 meses'});
});
