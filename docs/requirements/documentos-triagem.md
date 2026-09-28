# Correspondência com os documentos de triagem pediátrica

Esta especificação reúne os requisitos dos três materiais fornecidos em 24/09/2026. O aplicativo conserva React Native/Expo, o design existente (Nunito, mascote, cartões, cores e navegação) e PostgreSQL. A manutenção de Java/Spring Boot foi confirmada pelo solicitante; as sugestões de Node.js/Python não motivam uma migração.

## Fontes e interpretação

- **APP** — `projeto_triagem_pediatrica_app.pdf`, 3 páginas, seções 1–14.
- **AVANÇADO** — `projeto_triagem_pediatrica_avancado.pdf`, 3 páginas, seções 1–11.
- **SLIDES** — `apresentacao_triagem_pediatrica.pptx`, 10 slides.

Os materiais são requisitos de produto e exemplos de fluxo. Não demonstram validação clínica, certificação, redução comprovada de internações ou aprovação por sociedades médicas. Os benefícios indicados são objetivos do projeto. As tecnologias e provedores listados são alternativas, não uma exigência de instalar todos simultaneamente. A hospedagem em nuvem é prevista na arquitetura e no guia de infraestrutura; esta entrega permanece local.

## Matriz de requisitos funcionais

| Requisito | Origem | Aplicação e critério de aceite |
| --- | --- | --- |
| Apoiar pais, responsáveis e cuidadores | APP §§1–5; AVANÇADO §§1–2,11; SLIDES 1–3,10 | Sobre apresenta finalidade, público e limites; linguagem simples nos fluxos. |
| Crianças de 0 a 12 anos | APP §5 | Aceitar recém-nascido e criança antes do 13º aniversário; impedir idade futura/fora da faixa, inclusive ao avaliar. |
| Cadastro básico do usuário | APP §9; AVANÇADO §§3,8 | Cadastro, autenticação e perfil persistidos; senha protegida no servidor. |
| Cadastro da criança, peso opcional | APP §§6,9,12; AVANÇADO §§3,8 | Nome e idade obrigatórios, peso vazio permitido; nascimento exibido e digitado em DD/MM/AAAA com máscara e validação de calendário, mantendo ISO na API; preservar nascimento e peso ao editar. |
| Confirmar idade antes do sintoma | APP §11; SLIDES 4,7 | Avaliar → selecionar/confirmar criança e idade → sintoma. Sem criança, oferecer cadastro e retomar o fluxo. |
| Nove sintomas | APP §7; SLIDES 6 | Febre, tosse, vômitos, diarreia, dor abdominal, falta de ar, manchas na pele, trauma leve e dor de ouvido. |
| Questionário direcionado | APP §§4,6,9,11–12; AVANÇADO §3; SLIDES 3,7 | Perguntas específicas do sintoma, respostas explícitas, possibilidade de revisão e proteção contra envio duplo. |
| Processar respostas e classificar | APP §§8–9,11; AVANÇADO §§3–4; SLIDES 8 | Resultado calculado pela API, persistido e recuperável; sinais de emergência prevalecem. |
| Febre em menor de três meses | AVANÇADO §4 | Idade calculada a partir do nascimento cadastrado, sem repetir a pergunta de menor de três meses no questionário. Respostas enviadas por versões antigas não podem alterar o critério derivado da data. |
| Febre acima de 39°C | AVANÇADO §4 | Avaliação médica quando não há sinais de emergência; a temperatura isolada não deve disparar o antigo marcador de emergência. |
| Prostração e dificuldade respiratória | AVANÇADO §4; SLIDES 7 | Perguntas claras e classificação de atendimento imediato quando presentes. |
| Bom estado geral e observação | AVANÇADO §4 | Observação somente quando não há critérios de maior urgência; incerteza não equivale à ausência de alerta. |
| Três resultados acionáveis | APP §8; SLIDES 8 | Vermelho: atendimento imediato; amarelo: avaliação nas próximas horas, até 24h; verde: observação domiciliar e sinais para buscar ajuda. |
| Orientações e educação em saúde | APP §§4,9,12,14; AVANÇADO §§3,5,11; SLIDES 3,5,10 | Conteúdo para todos os sintomas, orientação contextual do resultado, detalhes legíveis e fontes acessíveis. |
| Histórico de avaliações | AVANÇADO §§3,5,7–8; SLIDES 5 | Persistência, filtro por criança, detalhe do resultado/data/sintoma e orientação associada. |
| Acessos da tela inicial | APP §12; SLIDES 5 | Avaliar, orientações, histórico e sobre acessíveis mantendo os componentes visuais. |
| Modelo usuário → criança → avaliação → sintoma/resultado | AVANÇADO §§7–8 | Relacionamentos persistentes no PostgreSQL; autorização restringe cada família aos próprios dados. |
| API de cadastro/sintomas/avaliação | AVANÇADO §9 | Rotas documentadas e autenticadas para crianças/avaliações; cadastro público validado. |

## Requisitos não funcionais

| Requisito | Implementação / validação |
| --- | --- |
| Interface simples e móvel (APP §10) | Preservação do tema e componentes; verificação em simulador iOS e testes de componentes. |
| Segurança e privacidade (APP §10) | Autenticação, autorização por proprietário, validação de entradas, armazenamento de sessão adequado à plataforma, ausência de detalhes internos em erros e conteúdo de privacidade verdadeiro. |
| Resposta rápida (APP §10) | API local com acesso ao banco e consultas agregadas; medir os fluxos reais de catálogo, questionário e avaliação. As medições locais não constituem SLA em produção. |
| Expansão futura (APP §10) | Catálogos no banco, migrações versionadas, separação entre apresentação, serviços e persistência; infraestrutura documentada. |
| Hospedagem em nuvem (APP §13; AVANÇADO §10; SLIDES 9) | Arquitetura e configuração por ambiente preservadas. Publicação, domínio e credenciais de nuvem dependem de ambiente escolhido; nenhuma publicação é realizada nesta tarefa. |

## Regras clínicas e rastreabilidade

O exemplo de febre dos materiais não especifica todos os limiares para cada faixa etária, nem protocolos completos para os outros oito sintomas. O aplicativo deve explicitar esse limite, conservar os sinais de alerta já existentes e registrar a versão e o motivo aplicados em cada nova avaliação. Os escores antigos não podem ser apresentados como probabilidade clínica.

A interpretação conservadora das respostas “Não sei” deve resultar em orientação para avaliação, sem converter a incerteza em uma resposta negativa. Informações obrigatórias ausentes devem ser rejeitadas. Sinais claros de emergência prevalecem sobre temperatura ou bom estado geral.

Referências primárias consultadas para conferir os sinais de alerta e a linguagem educativa:

- [NICE NG143 — Fever in under 5s: recommendations](https://www.nice.org.uk/guidance/ng143/chapter/recommendations): menos de três meses com temperatura ≥38°C exige atenção de alto risco; de três a seis meses com ≥39°C exige pelo menos avaliação intermediária. A referência se limita a menores de cinco anos e não valida o produto para toda a faixa 0–12.
- [NHS — High temperature in children](https://www.nhs.uk/symptoms/fever-in-children/): responsividade alterada, dificuldade respiratória e manchas que não desaparecem sob pressão exigem ação urgente.

A idade menor de três meses no fluxo de febre permanece uma condição conservadora do exemplo fornecido, incluindo suspeita de febre. Conteúdo clínico deve passar por revisão profissional antes de uso assistencial; a demonstração é educativa e não substitui consulta.

## Verificação

Os testes devem cobrir o fluxo autenticado completo, isolamento entre famílias, catálogo real dos nove sintomas, perguntas e alternativas realmente semeadas, limites de idade, prioridades do exemplo de febre, respostas desconhecidas, persistência do histórico e orientação correspondente. A verificação visual deve usar a aplicação com a API real, sem substituir erros por resultados simulados.


## Condições por idade cadastrada

Toda pergunta `YESNO` configurada com `idade_derivada=true` é resolvida no servidor pela data de nascimento da criança e não aparece no questionário. Sua configuração define limite inferior e/ou superior (`idade_min_valor` / `idade_max_valor`), unidade `DAYS`, `MONTHS` ou `YEARS` e inclusão de cada limite (`idade_min_inclusiva` / `idade_max_inclusiva`). Limites em meses e anos usam datas de aniversário no calendário; um limite ausente deve ter seus três campos nulos. Faixas vazias, invertidas ou configurações incompletas interrompem a avaliação, sem classificar o caso.

O mecanismo independe do código e do texto da pergunta e atende qualquer idade ou faixa configurada. A migração V8 configura apenas a condição já existente de menos de três meses; não cria limiares clínicos novos. Respostas legadas são substituídas pela resposta calculada, registrada com origem `CHILD_BIRTH_DATE`, mantendo as perguntas e avaliações históricas. Perguntas clínicas sem esses metadados continuam obrigatórias. A versão dessas regras é `projeto-2.2-educacional`.
