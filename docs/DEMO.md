# Demonstração do PediTriagem

O fluxo usa a API real e dados fictícios locais. O aplicativo é um projeto educativo de apoio à decisão; os documentos fornecem um exemplo de febre, não a validação clínica de todos os questionários.

## Preparação

1. Inicie PostgreSQL e a API conforme o README. Aplique as migrações Flyway, incluindo V7.
2. Inicie Expo com `EXPO_PUBLIC_API_URL=http://localhost:8080 npm start`.
3. No simulador iOS, abra o projeto no Expo Go. Para um celular físico, configure o IP da máquina como endereço da API e mantenha os dispositivos na mesma rede.
4. Crie uma conta de demonstração com informações fictícias. A tela de login da API real não presume que as credenciais de mock existem no banco.
5. Cadastre Maria, 4 anos, peso opcional de 17 kg, e Lucas, 2 meses, peso opcional. Não use dados de pacientes na apresentação.

## Fluxo principal

1. Mostre a tela inicial, a criança selecionada e os acessos a Avaliar, Orientações, Histórico e Sobre.
2. Toque em Avaliar sintomas e confirme a criança e a idade antes de escolher o sintoma.
3. Mostre os nove sintomas e a busca: febre, tosse, vômitos, diarreia, dor abdominal, falta de ar, manchas na pele, trauma leve e dor de ouvido.
4. Selecione Febre para Maria. Responda às perguntas e use os controles de avanço/revisão.
5. Para observação domiciliar, informe temperatura baixa, ausência de sinais de alerta, febre não persistente e bom estado geral. O resultado deve ser verde, com cuidados e critérios para procurar ajuda.
6. Repita para Maria com temperatura acima de 39°C, sem sinais de emergência e com bom estado geral. O resultado deve orientar avaliação nas próximas horas, em até 24 horas.
7. Repita indicando dificuldade respiratória ou prostração. O resultado deve indicar atendimento imediato. Mostre a ação de emergência sem realizar uma ligação real durante a gravação.
8. Selecione Lucas, de 2 meses, e avalie febre. A idade cadastrada deve prevalecer mesmo diante de resposta contraditória ao questionário: atendimento imediato.
9. Demonstre “Não sei”: informação incerta não gera tranquilização automática em verde.
10. Abra o histórico, filtre pela criança e consulte um resultado anterior. Data, nome e idade na avaliação são preservados mesmo após alterações no cadastro.
11. Abra Orientações, navegue pelos conteúdos de sintomas e confira as fontes. Mostre Sobre e Privacidade.

## Verificação antes da gravação

- API `/health` responde e conta consegue entrar.
- Cadastro aceita peso vazio, recém-nascido em meses e até 12 anos; rejeita 13 anos.
- Navegação passa pela confirmação de idade antes dos sintomas.
- Três resultados e histórico são persistidos pela API.
- Orientações de um resultado não são confundidas com conteúdo genérico de outro sintoma.
- Falhas de conexão aparecem com opção de tentar novamente; não são substituídas por resultados fictícios.

## Limites da demonstração

Os benefícios de reduzir ansiedade e procura desnecessária por emergência são objetivos, não resultados clínicos comprovados. Os escores internos não são probabilidades de doença. O aplicativo não fornece diagnóstico nem dose de medicamento. Hospedagem em nuvem e distribuição em lojas são etapas separadas da execução local.

A matriz de requisitos e as fontes estão em [documentos-triagem.md](requirements/documentos-triagem.md).
