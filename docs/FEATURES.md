# Features e Tasks — Detalhamento

> **Nota de status:** este documento é um plano histórico de tarefas. O contrato operacional atual do backend integrado está em [`openapi.yaml`](./openapi.yaml).

> Detalhamento de cada feature com suas tasks, critérios de aceitação e sugestões de atribuição.
> Para visão geral, ordem e dependências entre features, ver [`PLAN.md`](./PLAN.md).
> Para entender o visual de cada tela, ver a pasta [`design/`](../design/) no repositório (abrir `PediTriagem.html` no navegador para preview).

## Índice

- [Como ler este documento](#como-ler-este-documento)
- [F00 — Setup, Infraestrutura e Port do Design](#f00--setup-infraestrutura-e-port-do-design)
- [F01 — Autenticação](#f01--autenticação)
- [F02 — Cadastro e Gestão de Crianças](#f02--cadastro-e-gestão-de-crianças)
- [F03 — Catálogo de Sintomas](#f03--catálogo-de-sintomas)
- [F04 — Engine de Triagem](#f04--engine-de-triagem)
- [F05 — Resultado e Orientação](#f05--resultado-e-orientação)
- [F06 — Conteúdo Educativo (Orientações + About)](#f06--conteúdo-educativo-orientações--about)
- [F07 — Polimento, Build e Apresentação](#f07--polimento-build-e-apresentação)
- [F08 — Testes Automatizados](#f08--testes-automatizados)
- [F09 — Histórico de Avaliações](#f09--histórico-de-avaliações)
- [F10 — Tela de Perfil](#f10--tela-de-perfil)
- [Resumo de tasks por dev](#resumo-de-tasks-por-dev)
- [Como abrir as tasks como issues no GitHub](#como-abrir-as-tasks-como-issues-no-github)

---

## Como ler este documento

Cada **feature** começa com um cabeçalho que traz objetivo, prioridade, dependências e critérios de aceitação da feature inteira.

Cada **task** dentro da feature segue este formato:

```
#### F0X-T0Y — Título da task
- **Tipo:** backend | frontend | full-stack
- **Estimativa:** P (até 0.5 dia) | M (1-2 dias) | G (3-5 dias) | GG (>1 semana)
- **Sugestão de assignee:** <nome> (<usuario>)
- **Dependências:** <ids de outras tasks>
- **Labels:** <labels do GitHub para abrir issue>

**Descrição**
Contexto e objetivo da task.

**Tarefas**
- [ ] checklist do que fazer

**Critérios de aceitação**
- como saber que terminou
```

**Convenção de IDs:**
- Feature: `F00`, `F01`, ...
- Task: `F00-T01`, `F00-T02`, ...
- Branch: `feature/F01-T03-login-jwt` (use o id no início do nome da branch)

---

## F00 — Setup, Infraestrutura e Port do Design

> 🔴 **Crítica** • Esforço M+ • **Bloqueia tudo o que vem depois**

**Objetivo:** preparar o ambiente compartilhado E portar o design entregue para React Native, para que back e front trabalhem em paralelo com fidelidade visual desde o início.

**Critérios de aceitação da feature:**
- Backend sobe em `localhost:8080` sem erro
- Postgres dockerizado sobe via `docker-compose up -d` e backend conecta
- Swagger UI acessível em `/swagger-ui.html`
- App Expo abre com tab bar bottom (5 abas) + telas placeholder navegáveis
- Tokens de cor, fonte Nunito, mascote SVG e ícones funcionando no app
- CI roda em PRs e quebra se build falhar
- Documento de contrato OpenAPI publicado para o time

### Tasks

#### F00-T01 — Setup Postgres (docker-compose) + `application.properties`
- **Tipo:** backend
- **Estimativa:** M
- **Sugestão de assignee:** Ga (`ehoga`)
- **Dependências:** —
- **Labels:** `infra`, `backend`, `priority: high`

**Descrição**
Subir o Postgres em Docker para desenvolvimento local e configurar o Spring Boot para conectar. Cada dev sobe o banco com 1 comando. Sem H2.

**Tarefas**
- [ ] Criar `docker-compose.yml` na raiz do repo com serviço `postgres` (imagem `postgres:16`, porta 5432, volume nomeado para persistência, healthcheck)
- [ ] Variáveis: `POSTGRES_DB=triagem`, `POSTGRES_USER=triagem`, `POSTGRES_PASSWORD=triagem` (dev) — documentar para trocar em prod
- [ ] Adicionar dependência `org.postgresql:postgresql` no `pom.xml` (remover qualquer ref a H2)
- [ ] `application.properties` com `spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:triagem}`, user e password via env
- [ ] `spring.jpa.hibernate.ddl-auto=update` em dev (criação automática de tabelas)
- [ ] `server.port=8080`
- [ ] Variável `app.jwt.secret` com fallback de dev (alertar para trocar em prod)
- [ ] Variável `app.jwt.expiration-ms` (24h padrão)
- [ ] `.env.example` na raiz do `backend/` documentando variáveis
- [ ] Atualizar README com `docker-compose up -d` antes de subir o backend
- [ ] Adicionar `docker-compose.override.yml`, dados do volume e logs ao `.gitignore`

**Critérios de aceitação**
- `docker-compose up -d` sobe o Postgres
- `./mvnw spring-boot:run` conecta no banco e cria tabelas via JPA
- Sem dependência de H2 no projeto

---

#### F00-T02 — Habilitar CORS para o app mobile
- **Tipo:** backend
- **Estimativa:** P
- **Sugestão de assignee:** Ga (`ehoga`)
- **Dependências:** F00-T01
- **Labels:** `infra`, `backend`, `priority: high`

**Descrição**
Liberar CORS para que o Expo (rodando em outra origem) consiga consumir a API.

**Tarefas**
- [ ] Criar `WebConfig` com `CorsRegistry`
- [ ] Permitir `*` em ambiente dev
- [ ] Permitir métodos GET, POST, PUT, PATCH, DELETE, OPTIONS
- [ ] Permitir headers `Authorization`, `Content-Type`

**Critérios de aceitação**
- Requisição cross-origin a partir do app Expo retorna sem erro de CORS

---

#### F00-T03 — Configurar OpenAPI / Swagger UI
- **Tipo:** backend
- **Estimativa:** P
- **Sugestão de assignee:** Igor (`igorxdd`)
- **Dependências:** F00-T01
- **Labels:** `infra`, `backend`, `documentation`

**Descrição**
Adicionar Springdoc para gerar Swagger UI e usar como contrato vivo entre back e front. Decisão registrada no PLAN.md §13.

**Tarefas**
- [ ] Adicionar dep `springdoc-openapi-starter-webmvc-ui`
- [ ] Configurar título, descrição, versão da API
- [ ] Adicionar tag `bearerAuth` para JWT
- [ ] Conferir que endpoints aparecem após criar os primeiros controllers

**Critérios de aceitação**
- `http://localhost:8080/swagger-ui.html` lista os endpoints
- Botão "Authorize" funciona para inserir JWT
- `/v3/api-docs` retorna o JSON

---

#### F00-T04 — Endpoint de health check
- **Tipo:** backend
- **Estimativa:** P
- **Sugestão de assignee:** Saniel (`MSanielMartins`)
- **Dependências:** F00-T01
- **Labels:** `backend`, `infra`

**Descrição**
Endpoint público `GET /health` que retorna `{"status":"ok","timestamp":...}`. Útil para o app verificar se o backend está disponível.

**Tarefas**
- [ ] Criar `HealthController`
- [ ] Liberar no Spring Security (depois que F01 estiver pronto)
- [ ] Documentar no Swagger

**Critérios de aceitação**
- `curl localhost:8080/health` retorna 200 com JSON

---

#### F00-T05 — GitHub Actions: build/test do backend em PR
- **Tipo:** backend
- **Estimativa:** M
- **Sugestão de assignee:** Ga (`ehoga`)
- **Dependências:** F00-T01, F08-T01
- **Labels:** `infra`, `backend`

**Descrição**
Workflow que roda `./mvnw verify` em todo PR contra `dev` ou `main`. Os runners do GitHub Actions já têm Docker disponível, então Testcontainers funciona sem setup extra.

**Tarefas**
- [ ] Criar `.github/workflows/backend.yml`
- [ ] Trigger em `pull_request` com paths `backend/**`
- [ ] Setup do Java 21 (`actions/setup-java@v4`)
- [ ] Cache do Maven (`actions/cache` ou flag nativo do setup-java)
- [ ] Rodar `./mvnw -B verify`

**Critérios de aceitação**
- PR que quebra o build mostra ❌ no GitHub
- PR verde mostra ✅
- Testcontainers funciona no runner (Docker já está disponível por padrão)

---

#### F00-T06 — Configurar React Navigation com Bottom Tab Bar
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Caio (`caiomps`)
- **Dependências:** —
- **Labels:** `infra`, `frontend`, `priority: high`

**Descrição**
Configurar navegação base com **bottom tab navigator** (5 abas conforme design) + native stacks aninhadas. Telas placeholder para navegação funcionar antes das implementações reais.

**Tarefas**
- [ ] Instalar deps (`@react-navigation/native`, `@react-navigation/native-stack`, `@react-navigation/bottom-tabs`, `react-native-screens`, `react-native-safe-area-context`)
- [ ] Criar `src/navigation/RootNavigator.js` (decide entre AuthStack e MainTabs)
- [ ] Criar `MainTabs` com 5 abas: **Início**, **Avaliar** (destaque central), **Orientações**, **Histórico**, **Perfil**
- [ ] Stacks aninhadas em cada aba conforme necessário (ex: AvaliarStack: Symptoms → Quiz → Result)
- [ ] AuthStack: Login, Register
- [ ] Telas placeholder com `<Text>` apenas

**Critérios de aceitação**
- Tab bar aparece na parte inferior com 5 ícones
- Aba "Avaliar" tem visual destacado (botão central elevado, conforme design)
- Navegação entre abas e entre telas dentro de cada stack funciona

---

#### F00-T07 — Port dos tokens de design para React Native
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Matheus (`jomatheusdev`)
- **Dependências:** F00-T06
- **Labels:** `infra`, `frontend`, `priority: high`

**Descrição**
Portar os design tokens definidos em `design/peditriagem-tokens.jsx` para uma estrutura idiomática de React Native.

**Tarefas**
- [ ] `src/theme/colors.js` (primary, navy, secondary, lowSolid, modSolid, highSolid, soft/softer, surfaces, text)
- [ ] `src/theme/typography.js` (Nunito 400/600/700/800/900) — instalar fonte via `expo-font` ou `@expo-google-fonts/nunito`
- [ ] `src/theme/spacing.js` (escala 4/8/12/16/20/24)
- [ ] `src/theme/radii.js` (10/12/14/16/18/20/24)
- [ ] `src/theme/shadows.js` (shadow padrão e shadowLg, usando `elevation` no Android e `shadowOffset` no iOS)
- [ ] Tela de "Storybook visual" simples (rota `/dev` mostrando todos os componentes)

**Critérios de aceitação**
- Componentes usam o tema (sem hex/string hardcoded nas telas)
- Fonte Nunito carrega no app

---

#### F00-T08 — Port dos componentes base para React Native
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Matheus (`jomatheusdev`)
- **Dependências:** F00-T07
- **Labels:** `infra`, `frontend`, `priority: high`

**Descrição**
Portar componentes definidos no design (Card, Pill, PrimaryButton, GhostButton, ScreenHeader, ProgressBar) para componentes RN.

**Tarefas**
- [ ] `src/components/Card.js`
- [ ] `src/components/Pill.js` (variantes primary/low/mod/high/neutral)
- [ ] `src/components/PrimaryButton.js` (variantes primary/navy/low/mod/high)
- [ ] `src/components/GhostButton.js`
- [ ] `src/components/ScreenHeader.js`
- [ ] `src/components/ProgressBar.js`
- [ ] Todos com props compatíveis com o design

**Critérios de aceitação**
- Cada componente renderiza visualmente igual ao protótipo
- Storybook visual (F00-T07) inclui exemplos de cada um

---

#### F00-T09 — Port dos ícones SVG do design
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Pedro (`PedroLucasSCPB`)
- **Dependências:** F00-T07
- **Labels:** `infra`, `frontend`

**Descrição**
Portar os 25+ ícones SVG (9 sintomas + 16 UI) de `design/peditriagem-tokens.jsx` para componentes RN usando `react-native-svg`.

**Tarefas**
- [ ] Instalar `react-native-svg`
- [ ] `src/components/icons/` com um arquivo por ícone OU um único arquivo `Icon.js` exportando todos
- [ ] Ícones de sintomas: thermo, cough, vomit, drop, belly, lung, rash, bandage, ear
- [ ] Ícones de UI: home, stetho, book, history, user, back, chevR, chevD, close, plus, check, bell, search, shield, info, warn, alert, phone, heart, bed, pill
- [ ] Suportar props `size`, `color`, `sw` (stroke width)

**Critérios de aceitação**
- Todos os ícones renderizam no Storybook visual
- Stroke width customizável via prop

---

#### F00-T10 — Implementar componente Mascote
- **Tipo:** frontend
- **Estimativa:** P
- **Sugestão de assignee:** Andre (`Andrelbf41`)
- **Dependências:** F00-T07
- **Labels:** `infra`, `frontend`

**Descrição**
Portar o `Mascot` SVG do design para componente RN com 3 moods: `calm` (azul), `watch` (amarelo), `alert` (vermelho).

**Tarefas**
- [ ] `src/components/Mascot.js` usando `react-native-svg`
- [ ] Props: `size`, `mood`
- [ ] Gradient radial via `Defs` + `RadialGradient` do `react-native-svg`

**Critérios de aceitação**
- Mascote renderiza nos 3 moods
- Tamanho customizável via prop

---

#### F00-T11 — Cliente HTTP (axios) + interceptor JWT
- **Tipo:** frontend
- **Estimativa:** P
- **Sugestão de assignee:** Matheus (`jomatheusdev`)
- **Dependências:** F00-T08
- **Labels:** `infra`, `frontend`

**Descrição**
Configurar axios apontando para a API com base URL via env, interceptor que adiciona `Authorization: Bearer <token>` quando disponível.

**Tarefas**
- [ ] Instalar `axios`
- [ ] `src/services/api.js` com instância configurada
- [ ] Base URL via `process.env.EXPO_PUBLIC_API_URL` com fallback para `http://localhost:8080`
- [ ] Interceptor de request: anexa JWT do AsyncStorage
- [ ] Interceptor de response: trata 401 (limpa storage e redireciona para Login)

**Critérios de aceitação**
- Chamada de teste para `/health` funciona
- Sem token, requisições são feitas sem header `Authorization`

---

#### F00-T12 — Helper de AsyncStorage
- **Tipo:** frontend
- **Estimativa:** P
- **Sugestão de assignee:** Matheus (`jomatheusdev`)
- **Dependências:** —
- **Labels:** `infra`, `frontend`

**Descrição**
Wrapper para `@react-native-async-storage/async-storage` com chaves padronizadas.

**Tarefas**
- [ ] Instalar dep
- [ ] `src/services/storage.js` com `getToken`, `setToken`, `clearAuth`, `getUser`, `setUser`, `getDisclaimerAcepto`, `setDisclaimerAcepto`
- [ ] Constantes para chaves em `src/services/storageKeys.js`

**Critérios de aceitação**
- Funções retornam `null` quando não há valor (sem throw)

---

#### F00-T13 — GitHub Actions: build do frontend em PR
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Matheus (`jomatheusdev`)
- **Dependências:** F00-T06
- **Labels:** `infra`, `frontend`

**Descrição**
Workflow que roda `npm install` e `npx expo doctor` no frontend em PRs.

**Tarefas**
- [ ] Criar `.github/workflows/frontend.yml`
- [ ] Trigger em PRs com paths `frontend/**`
- [ ] Setup Node 20
- [ ] Cache do `~/.npm`
- [ ] Rodar `npm ci` e `npx expo doctor`

**Critérios de aceitação**
- PR no front executa o workflow

---

#### F00-T14 — Definir contrato OpenAPI inicial (alinhamento back ↔ front)
- **Tipo:** full-stack
- **Estimativa:** M
- **Sugestão de assignee:** Ga (`ehoga`) + Matheus (`jomatheusdev`)
- **Dependências:** F00-T03
- **Labels:** `infra`, `documentation`, `priority: high`

**Descrição**
Documentar e versionar o contrato dos endpoints planejados **antes** de back e front começarem a se integrar. Isso evita retrabalho.

**Tarefas**
- [ ] Listar endpoints com método, path, request body, response body, códigos de erro
- [ ] Salvar versão exportada do swagger em `docs/openapi.yaml` ou similar
- [ ] Reunião curta entre tech leads para validar

**Critérios de aceitação**
- Arquivo `openapi.yaml` existe com endpoints do MVP descritos
- Front não inventa endpoint sem antes ser refletido aqui

---

## F01 — Autenticação

> 🔴 **Crítica** • Esforço G • **Depende de:** F00

**Objetivo:** usuário consegue criar conta e fazer login. Token JWT protege endpoints que vêm depois.

**Critérios de aceitação da feature:**
- Cadastro com email único + senha (mínimo 6 caracteres)
- Login retorna JWT válido por 24h
- Endpoints `/api/**` exigem JWT; `/auth/**` e `/health` são públicos
- App persiste token e mantém usuário logado entre sessões
- Telas seguem o sistema visual definido em F00 (Card, PrimaryButton, etc.)

### Tasks

#### F01-T01 — Modelar entidade Usuario
- **Tipo:** backend
- **Estimativa:** P
- **Sugestão de assignee:** Saniel (`MSanielMartins`)
- **Dependências:** F00-T01
- **Labels:** `backend`, `feature`

**Descrição**
JPA entity para usuário, com Bean Validation.

**Tarefas**
- [ ] Campos: `id`, `nome`, `email` (único, validado), `senha` (hash), `criadoEm`
- [ ] Validações no DTO: email formato, nome 2-100 chars, senha 6-100 chars
- [ ] DDL automático em dev

**Critérios de aceitação**
- Entity persiste em Postgres
- Email duplicado rejeita com erro tratado

---

#### F01-T02 — Configurar Spring Security + filtro JWT
- **Tipo:** backend
- **Estimativa:** G
- **Sugestão de assignee:** Saniel (`MSanielMartins`)
- **Dependências:** F01-T01
- **Labels:** `backend`, `feature`, `priority: high`

**Descrição**
Habilitar Spring Security 6 com filtro customizado que valida JWT em cada requisição.

**Tarefas**
- [ ] Adicionar `spring-boot-starter-security` e `jjwt-api`/`impl`/`jackson` (0.12+)
- [ ] `SecurityConfig` com `SecurityFilterChain`
- [ ] `JwtAuthenticationFilter` que extrai token do header e popula SecurityContext
- [ ] `JwtService` com `generateToken(Usuario)` e `validateToken(String)`
- [ ] Rotas públicas: `/auth/**`, `/health`, `/swagger-ui.html`, `/v3/api-docs/**`
- [ ] CSRF desabilitado (API stateless)
- [ ] Sessão `STATELESS`

**Critérios de aceitação**
- Requisição sem JWT em rota protegida retorna 401
- JWT válido permite acesso
- JWT expirado retorna 401

---

#### F01-T03 — Endpoint POST `/auth/register`
- **Tipo:** backend
- **Estimativa:** M
- **Sugestão de assignee:** Saniel (`MSanielMartins`)
- **Dependências:** F01-T02
- **Labels:** `backend`, `feature`

**Descrição**
Cadastro de novo usuário. Retorna o usuário criado (sem senha) e o JWT.

**Tarefas**
- [ ] DTO `RegisterRequest` com validações
- [ ] DTO `AuthResponse` com `{usuario: {id, nome, email}, token}`
- [ ] `AuthController` + `AuthService`
- [ ] Bcrypt para hash da senha
- [ ] Retorna 409 se email duplicado, 400 se validação falhar

**Critérios de aceitação**
- POST com payload válido retorna 201 + JWT
- POST com email existente retorna 409
- Senha nunca aparece na resposta

---

#### F01-T04 — Endpoint POST `/auth/login`
- **Tipo:** backend
- **Estimativa:** M
- **Sugestão de assignee:** Saniel (`MSanielMartins`)
- **Dependências:** F01-T03
- **Labels:** `backend`, `feature`

**Descrição**
Autentica usuário e retorna JWT.

**Tarefas**
- [ ] DTO `LoginRequest` (email, senha)
- [ ] Verificar senha com bcrypt
- [ ] Retorna 401 em credenciais inválidas (sem dizer se foi email ou senha)

**Critérios de aceitação**
- Login válido retorna 200 + JWT
- Email inexistente ou senha errada → 401

---

#### F01-T05 — Endpoint GET `/auth/me`
- **Tipo:** backend
- **Estimativa:** P
- **Sugestão de assignee:** Saniel (`MSanielMartins`)
- **Dependências:** F01-T04
- **Labels:** `backend`, `feature`

**Descrição**
Retorna dados do usuário autenticado (útil para rehidratar contexto no app e para a tela Perfil).

**Tarefas**
- [ ] Lê `Authentication` do SecurityContext
- [ ] Retorna `{id, nome, email}`

**Critérios de aceitação**
- Sem JWT → 401
- JWT válido → 200 com dados do usuário

---

#### F01-T06 — Tratamento de erros padronizado
- **Tipo:** backend
- **Estimativa:** M
- **Sugestão de assignee:** Saniel (`MSanielMartins`)
- **Dependências:** F01-T03
- **Labels:** `backend`, `infra`

**Descrição**
`@RestControllerAdvice` global que padroniza respostas de erro.

**Tarefas**
- [ ] DTO `ApiError` com `{status, mensagem, campos: [{campo, erro}]}`
- [ ] Handlers para `MethodArgumentNotValidException`, `BadCredentialsException`, `EntityNotFoundException`, `Exception` genérico

**Critérios de aceitação**
- Validação retorna lista de campos com erro
- Frontend consegue mapear erros de campo facilmente

---

#### F01-T07 — Tela de Login
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Caio (`caiomps`)
- **Dependências:** F00-T08, F00-T11
- **Labels:** `frontend`, `feature`

**Descrição**
Tela com email e senha, usando o sistema visual do design (Card, PrimaryButton, GhostButton). Header com nome do app + mascote.

**Tarefas**
- [ ] Form com validação client-side
- [ ] Loading durante submit (estado no PrimaryButton)
- [ ] Tratamento de erro 401 (mensagem amigável)
- [ ] Após sucesso, salva token e navega para MainTabs
- [ ] Link "Criar conta" no rodapé

**Critérios de aceitação**
- Login com credenciais válidas leva o usuário para a Home
- Erros são mostrados sem crashar o app
- Visual usa cores e tipografia do tema

---

#### F01-T08 — Tela de Cadastro
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Caio (`caiomps`)
- **Dependências:** F01-T07
- **Labels:** `frontend`, `feature`

**Descrição**
Tela com nome, email, senha e confirmação. Cria conta e já loga o usuário.

**Tarefas**
- [ ] Validação client-side (senhas batem, email válido)
- [ ] Submit chama `/auth/register`
- [ ] Após sucesso, navega para MainTabs (já autenticado)
- [ ] Tratamento de 409 (email já existe)
- [ ] Link "Já tenho conta" leva para Login

**Critérios de aceitação**
- Cadastro válido cria usuário e loga automaticamente
- Email duplicado mostra mensagem clara

---

#### F01-T09 — AuthContext + hook `useAuth`
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Matheus (`jomatheusdev`)
- **Dependências:** F00-T12, F01-T07
- **Labels:** `frontend`, `feature`

**Descrição**
Context que expõe `user`, `token`, `login()`, `register()`, `logout()`. Hidrata estado a partir do AsyncStorage no boot do app.

**Tarefas**
- [ ] `src/contexts/AuthContext.js`
- [ ] No mount, lê token do storage e chama `/auth/me` para validar
- [ ] Expõe `isLoading` para evitar flash da tela errada
- [ ] `logout()` limpa storage e estado

**Critérios de aceitação**
- App reaberto mantém usuário logado se token válido
- Token expirado → desloga e leva para Login

---

#### F01-T10 — Guard de rotas autenticadas
- **Tipo:** frontend
- **Estimativa:** P
- **Sugestão de assignee:** Matheus (`jomatheusdev`)
- **Dependências:** F01-T09
- **Labels:** `frontend`, `feature`

**Descrição**
RootNavigator decide qual stack mostrar (Auth ou MainTabs) com base no `user` do AuthContext.

**Tarefas**
- [ ] Splash enquanto AuthContext está carregando
- [ ] Sem login → AuthStack (Login, Register)
- [ ] Logado → MainTabs (com 5 abas)

**Critérios de aceitação**
- Sem login → só vê Auth screens
- Logado → entra direto no MainTabs

---

## F02 — Cadastro e Gestão de Crianças

> 🟠 **Alta** • Esforço M • **Depende de:** F01

**Objetivo:** usuário gerencia as crianças que serão avaliadas (1 ou mais), cada uma com avatar emoji.

**Critérios de aceitação da feature:**
- CRUD completo de criança vinculada ao usuário do JWT
- Usuário só vê e altera as próprias crianças
- Cada criança tem nome, dataNascimento, peso (opcional) e avatar emoji
- Idade exibida em "X meses" se < 24 meses, senão em "X anos"

### Tasks

#### F02-T01 — Modelar entidade Crianca
- **Tipo:** backend
- **Estimativa:** P
- **Sugestão de assignee:** Ga (`ehoga`)
- **Dependências:** F01-T01
- **Labels:** `backend`, `feature`

**Descrição**
Entity com FK para Usuario. Idade armazenada como `dataNascimento` (LocalDate).

**Tarefas**
- [ ] Campos: `id`, `nome`, `dataNascimento` (LocalDate), `pesoKg` (BigDecimal, opcional), `avatarEmoji` (String, default "🌸"), `usuario_id`
- [ ] Validações: nome obrigatório, data não no futuro, criança até 12 anos (validar no service), avatarEmoji entre opções permitidas
- [ ] Relação `@ManyToOne` para Usuario
- [ ] Helper `int idadeEmMeses()` no DTO de resposta para front exibir corretamente

**Critérios de aceitação**
- Persiste em Postgres com FK funcionando
- Cascade DELETE se o usuário for removido

---

#### F02-T02 — Endpoints REST de Crianca (CRUD)
- **Tipo:** backend
- **Estimativa:** G
- **Sugestão de assignee:** Ga (`ehoga`)
- **Dependências:** F02-T01
- **Labels:** `backend`, `feature`

**Descrição**
Implementar `POST /api/criancas`, `GET /api/criancas`, `GET /api/criancas/{id}`, `PUT /api/criancas/{id}`, `DELETE /api/criancas/{id}`.

**Tarefas**
- [ ] DTOs `CriancaRequest` e `CriancaResponse` (com `idadeEmMeses` calculada)
- [ ] Service que **filtra por usuario do JWT** em todas as operações
- [ ] 404 se a criança não pertencer ao usuário (não vazar info)
- [ ] Validar lista permitida de avatares (🌸, 🌱, ⭐, 🐻, 🦊, 🌈)
- [ ] Suítes de teste (ver F08)

**Critérios de aceitação**
- Usuário A não consegue listar nem alterar criança do usuário B
- Validações de payload retornam 400

---

#### F02-T03 — Helper de cálculo de idade no frontend
- **Tipo:** frontend
- **Estimativa:** P
- **Sugestão de assignee:** Jefferson (`jeffersonEzequiel`)
- **Dependências:** F00-T11
- **Labels:** `frontend`, `feature`

**Descrição**
Função utilitária que converte `dataNascimento` em string "X meses" ou "X anos" conforme idade.

**Tarefas**
- [ ] `src/utils/age.js` com `formatAge(dataNascimento)`
- [ ] Se < 24 meses, retorna "X meses"
- [ ] Senão, retorna "X anos"
- [ ] Testes unitários simples

**Critérios de aceitação**
- "8 meses" para 8 meses de vida
- "4 anos" para criança de 4 anos

---

#### F02-T04 — Tela de adicionar/editar criança
- **Tipo:** frontend
- **Estimativa:** G
- **Sugestão de assignee:** Caio (`caiomps`)
- **Dependências:** F02-T03, F00-T11
- **Labels:** `frontend`, `feature`

**Descrição**
Formulário fiel ao design: avatar emoji selecionável (6 opções), nome, idade (toggle meses/anos + número), peso opcional. Reaproveitada para criar e editar.

**Tarefas**
- [ ] Avatar selector grande no topo + grid de 6 opções (🌸, 🌱, ⭐, 🐻, 🦊, 🌈)
- [ ] Input nome
- [ ] Input idade numérica + toggle `meses`/`anos` (converter para `dataNascimento` antes de enviar)
- [ ] Input peso opcional com sufixo "kg"
- [ ] Botão "Salvar e continuar" → POST/PUT
- [ ] Após sucesso, navega de volta

**Critérios de aceitação**
- Cria criança válida com todos os campos
- Edita criança existente
- Validações exibem mensagens claras
- Conversão idade → dataNascimento correta

---

#### F02-T05 — Confirmação de exclusão
- **Tipo:** frontend
- **Estimativa:** P
- **Sugestão de assignee:** Caio (`caiomps`)
- **Dependências:** F02-T04
- **Labels:** `frontend`, `feature`

**Descrição**
Botão "Excluir" na edição. Confirmação via Alert nativo antes de deletar.

**Tarefas**
- [ ] Alert com cancelar/confirmar
- [ ] Chama DELETE
- [ ] Volta para lista (na tela Perfil)

**Critérios de aceitação**
- Exclusão exige confirmação
- Após excluir, lista é atualizada

---

## F03 — Catálogo de Sintomas

> 🟠 **Alta** • Esforço P • **Depende de:** F00

**Objetivo:** disponibilizar a lista dos 9 sintomas para escolha, com busca e card de emergência.

**Critérios de aceitação da feature:**
- Backend serve os 9 sintomas via API com ícone e cor
- Frontend mostra lista com busca + card de emergência fixo

### Tasks

#### F03-T01 — Modelar entidade Sintoma + seed
- **Tipo:** backend
- **Estimativa:** M
- **Sugestão de assignee:** Igor (`igorxdd`)
- **Dependências:** F00-T01
- **Labels:** `backend`, `feature`

**Descrição**
Entity Sintoma e seed inicial com os 9 sintomas no startup.

**Tarefas**
- [ ] Campos: `id`, `codigo` (slug, ex: "fever"), `nome`, `descricaoCurta`, `iconeRef` (string, ex: "thermo"), `corHex` (string, ex: "#F59E5C")
- [ ] Seed: fever (Febre), cough (Tosse), vomit (Vômitos), diarrhea (Diarreia), belly (Dor abdominal), breath (Falta de ar), rash (Manchas na pele), trauma (Trauma leve), ear (Dor de ouvido)
- [ ] Cada sintoma com descrição curta, ícone e cor conforme `design/peditriagem-screens-a.jsx`

**Critérios de aceitação**
- Após start, banco tem 9 registros de sintoma com todos os campos
- Cores e ícones correspondem ao design

---

#### F03-T02 — Endpoint `GET /api/sintomas`
- **Tipo:** backend
- **Estimativa:** P
- **Sugestão de assignee:** Igor (`igorxdd`)
- **Dependências:** F03-T01
- **Labels:** `backend`, `feature`

**Descrição**
Lista todos os sintomas. Endpoint autenticado.

**Tarefas**
- [ ] Controller + Service
- [ ] DTO `SintomaResponse` (todos os campos do design)
- [ ] Documentar no Swagger

**Critérios de aceitação**
- Retorna 9 itens com nome, descrição, ícone e cor

---

#### F03-T03 — Tela "Selecionar sintoma" (com busca + card emergência)
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Pedro (`PedroLucasSCPB`)
- **Dependências:** F03-T02, F00-T08, F00-T09
- **Labels:** `frontend`, `feature`

**Descrição**
Tela com search bar no topo, grid 2 colunas com 9 sintomas (cada um com ícone e cor do design) + card de emergência 192 fixo no rodapé. Após escolher a criança, usuário vê esta tela.

**Tarefas**
- [ ] Hook `useSintomas`
- [ ] Search bar filtra por nome em tempo real
- [ ] Componente `SintomaCard` (ícone tintado + nome + descrição curta)
- [ ] Card de "Emergência?" com botão "Ligar 192" usando `Linking.openURL("tel:192")`
- [ ] Tap no sintoma navega para tela de questionário com `sintomaId` e `criancaId`
- [ ] Estado de loading e erro

**Critérios de aceitação**
- Busca filtra a lista corretamente
- Card de emergência abre discador no celular
- Tap navega para Quiz com parâmetros corretos

---

## F04 — Engine de Triagem

> 🔴 **Crítica** • Esforço GG • **Depende de:** F02 e F03 • **Coração do produto**

**Objetivo:** classificar o risco da criança usando **scoring (pesos por resposta) + red flags clínicos**. Red flags têm prioridade: qualquer resposta marcada como red flag classifica como ALTO RISCO independente do score.

**Critérios de aceitação da feature:**
- Engine implementado com regra clara: red flag → HIGH; senão score → faixa
- 9 sintomas com perguntas, pesos e red flags definidos
- Endpoint POST de avaliação retorna classificação + score + red flag detected
- Frontend renderiza dinamicamente o questionário (options ou yesno+dunno) com mascote em bubble
- Engine coberto por testes unitários (3 níveis × 9 sintomas + casos de red flag)

**Modelo conceitual:**
```
Pergunta {
  id, sintomaId, codigo, texto, sub (descrição curta), tipo, ordem
  tipo ∈ {OPTIONS, YESNO}
}

Opcao {  // para tipo OPTIONS
  perguntaId, codigo, texto, score (int 0-3), redFlag (bool)
}

PesoYesNo {  // para tipo YESNO
  perguntaId, scoreYes, scoreNo, scoreDunno, redFlagOnYes, redFlagOnNo
}

Engine:
  if any answer triggered redFlag → return HIGH
  else totalScore = sum of scores → map:
    < 3 → LOW
    3-5 → MOD
    >= 6 → HIGH
```

### Tasks

#### F04-T01 — Modelar Pergunta, Opção e PesoYesNo
- **Tipo:** backend
- **Estimativa:** G
- **Sugestão de assignee:** Igor (`igorxdd`)
- **Dependências:** F03-T01
- **Labels:** `backend`, `feature`, `priority: high`

**Descrição**
Modelar a estrutura de questionário usando scoring + red flags.

**Tarefas**
- [ ] Entity `Pergunta`: `id`, `sintoma_id`, `codigo`, `texto`, `sub`, `tipo` (OPTIONS | YESNO), `ordem`
- [ ] Entity `OpcaoPergunta` (para tipo OPTIONS): `id`, `pergunta_id`, `codigo`, `texto`, `score` (int), `redFlag` (bool, default false)
- [ ] Entity `PesoYesNo` (para tipo YESNO): `id`, `pergunta_id`, `scoreYes`, `scoreNo`, `scoreDunno`, `redFlagOnYes` (bool), `redFlagOnNo` (bool, raro mas possível)
- [ ] Documentar formato no código

**Critérios de aceitação**
- Modelo permite descrever o quiz de febre do design
- Modelo suporta red flag em qualquer resposta

---

#### F04-T02 — Modelar entidade Avaliacao
- **Tipo:** backend
- **Estimativa:** P
- **Sugestão de assignee:** Igor (`igorxdd`)
- **Dependências:** F04-T01
- **Labels:** `backend`, `feature`

**Descrição**
Persiste cada avaliação realizada (necessária para F09 Histórico).

**Tarefas**
- [ ] Entity `Avaliacao`: `id`, `crianca_id`, `sintoma_id`, `respostas` (JSONB: lista de `{perguntaCodigo, valor}`), `classificacao` (LOW | MOD | HIGH), `score` (int), `redFlagDetected` (bool), `criadoEm` (timestamp)
- [ ] Mapear `respostas` como `jsonb` no Postgres (usar `@JdbcTypeCode(SqlTypes.JSON)` do Hibernate 6 ou converter manualmente)
- [ ] Auditoria (`criadoEm` automático)

**Critérios de aceitação**
- Persiste em Postgres com coluna JSONB
- Round-trip de respostas (gravar e recuperar) preserva estrutura

---

#### F04-T03 — TriagemService (engine de scoring + red flags)
- **Tipo:** backend
- **Estimativa:** G
- **Sugestão de assignee:** Igor (`igorxdd`)
- **Dependências:** F04-T01
- **Labels:** `backend`, `feature`, `priority: high`

**Descrição**
Lógica do engine. Recebe `(sintomaId, respostas[])` e retorna classificação.

**Tarefas**
- [ ] Algoritmo:
  1. Para cada resposta, busca a pergunta + opção/peso
  2. Se a resposta marcou redFlag → marca `redFlagDetected = true`
  3. Soma scores
  4. Se redFlagDetected → classificação = HIGH
  5. Senão: < 3 → LOW; 3-5 → MOD; >= 6 → HIGH
- [ ] Retorna `{classificacao, score, redFlagDetected, recomendacao, chips: [3 itens de ação]}`
- [ ] Texto de recomendação e chips são pré-definidos por nível (não dependem do sintoma para o MVP)
- [ ] Documentar com Javadoc

**Critérios de aceitação**
- Casos de teste cobrem: red flag → HIGH; score 0-2 → LOW; 3-5 → MOD; ≥6 → HIGH
- Cobertura ≥ 80% no service

---

#### F04-T04 — Endpoint `GET /api/sintomas/{id}/perguntas`
- **Tipo:** backend
- **Estimativa:** P
- **Sugestão de assignee:** Igor (`igorxdd`)
- **Dependências:** F04-T01, F03-T02
- **Labels:** `backend`, `feature`

**Descrição**
Retorna o questionário completo de um sintoma para o front renderizar.

**Tarefas**
- [ ] DTO com perguntas + opções/pesos
- [ ] **Não** expor `redFlag` nem `score` no response (front não precisa saber — engine faz no back)
- [ ] Cache em memória (sintomas e perguntas mudam raramente)

**Critérios de aceitação**
- Front consegue reconstruir o quiz a partir do retorno
- Campos sensíveis (scores, redFlags) não vazam para o front

---

#### F04-T05 — Endpoint `POST /api/avaliacoes`
- **Tipo:** backend
- **Estimativa:** M
- **Sugestão de assignee:** Igor (`igorxdd`)
- **Dependências:** F04-T03, F04-T02, F02-T02
- **Labels:** `backend`, `feature`

**Descrição**
Recebe `{criancaId, sintomaId, respostas: [{perguntaCodigo, valor}]}`, processa, persiste e retorna resultado.

**Tarefas**
- [ ] DTOs request/response
- [ ] Valida que a criança pertence ao user do JWT
- [ ] Chama `TriagemService`
- [ ] Persiste `Avaliacao`
- [ ] Retorna `{id, classificacao, score, recomendacao, chips, criadoEm, crianca, sintoma}`

**Critérios de aceitação**
- POST com payload válido retorna classificação correta
- Tentativa de avaliar criança de outro user → 404

---

#### F04-T06 a F04-T14 — Seeds dos 9 questionários

> Cada um desses é uma task específica. Fazer **um sintoma por task** facilita o paralelismo.
> O quiz da febre já está modelado no design (`design/peditriagem-screens-b.jsx` linha 3-47) e serve como **referência**.

| ID | Sintoma | Estimativa | Assignee sugerido | Red flags principais |
|---|---|---|---|---|
| **F04-T06** | Febre (referência) | M | Igor (`igorxdd`) | Idade < 3 meses; petéquias; respiração difícil; prostração extrema |
| **F04-T07** | Tosse | M | Igor (`igorxdd`) | Cianose (lábios azulados); apneia; sibilância grave |
| **F04-T08** | Vômitos | M | Saniel (`MSanielMartins`) | Vômito com sangue; sinais de desidratação grave; vômito biliar |
| **F04-T09** | Diarreia | M | Saniel (`MSanielMartins`) | Sangue nas fezes; desidratação grave; bebê < 6 meses |
| **F04-T10** | Dor abdominal | M | Ga (`ehoga`) | Dor intensa contínua; abdome em tábua; sangue nas fezes |
| **F04-T11** | Falta de ar | M | Igor (`igorxdd`) | Cianose; tiragem grave; FR muito elevada; criança incapaz de falar |
| **F04-T12** | Manchas na pele | M | Ga (`ehoga`) | Petéquias que não somem ao apertar; febre alta + manchas |
| **F04-T13** | Trauma leve | M | Saniel (`MSanielMartins`) | Perda de consciência; sangramento que não para; deformidade |
| **F04-T14** | Dor de ouvido | M | Ga (`ehoga`) | Secreção purulenta abundante; febre alta + dor intensa |

**Padrão para cada uma dessas tasks:**

> ##### F04-T0X — Seed questionário: \<sintoma\>
> - **Tipo:** backend
> - **Estimativa:** M
> - **Dependências:** F04-T01, F04-T03 (engine pronto), F04-T06 (febre como referência)
> - **Labels:** `backend`, `feature`
>
> **Descrição**
> Pesquisar protocolo simplificado para o sintoma e codificar 4-6 perguntas com scores e red flags.
>
> **Tarefas**
> - [ ] Definir 4-6 perguntas que cobrem os 3 níveis de risco
> - [ ] Identificar pelo menos 1 red flag clínico
> - [ ] Codificar como seed (data.sql ou seeder Java)
> - [ ] Adicionar 1 caso de teste por nível + 1 caso de red flag em F08
> - [ ] Documentar fonte do protocolo no commit (Sociedade Brasileira de Pediatria, Ministério da Saúde, etc.)
>
> **Critérios de aceitação**
> - Os 3 níveis de risco são alcançáveis em pelo menos 1 cenário
> - Red flag dispara HIGH corretamente
> - Engine processa o sintoma sem erro

---

#### F04-T15 — Tela de Questionário (renderização dinâmica)
- **Tipo:** frontend
- **Estimativa:** GG
- **Sugestão de assignee:** Jefferson (`jeffersonEzequiel`)
- **Dependências:** F03-T03, F04-T04, F00-T10 (Mascote)
- **Labels:** `frontend`, `feature`, `priority: high`

**Descrição**
Tela do quiz com pergunta atual em **bubble com mascote**, conforme design (linhas 88-167 de `screens-b.jsx`). Renderiza dinamicamente com base no tipo da pergunta retornado pelo back.

**Tarefas**
- [ ] Carrega `GET /api/sintomas/{id}/perguntas`
- [ ] Estado: pergunta atual (index), histórico de respostas
- [ ] Header com nome do sintoma + criança + ProgressBar (etapa X/Y)
- [ ] Mascote (`mood="calm"`) + bubble com texto da pergunta
- [ ] Renderizadores por tipo:
  - `OPTIONS`: lista vertical de opções (botões grandes, single-select)
  - `YESNO`: grid 2 colunas (Sim/Não) + botão largura total "Não tenho certeza"
- [ ] Avanço automático após selecionar resposta (200ms delay)
- [ ] Botão "Voltar" no header desfaz última resposta
- [ ] Texto de privacidade no rodapé: "🔒 Suas respostas ficam apenas neste dispositivo."
- [ ] Ao terminar, chama `POST /api/avaliacoes` com respostas e navega para resultado

**Critérios de aceitação**
- Funciona para os 9 sintomas
- Visual fiel ao design
- Resposta seleção tem feedback visual (cor primária)
- Ramificação não é necessária — todas as perguntas são apresentadas em ordem

---

#### F04-T16 — Loading e tratamento de erro do questionário
- **Tipo:** frontend
- **Estimativa:** P
- **Sugestão de assignee:** Jefferson (`jeffersonEzequiel`)
- **Dependências:** F04-T15
- **Labels:** `frontend`, `feature`

**Descrição**
Skeleton durante carregamento do quiz. Tela de erro com retry se a API falhar.

**Tarefas**
- [ ] Skeleton da tela
- [ ] Erro com botão "Tentar novamente"
- [ ] Cancelar e voltar para sintomas

**Critérios de aceitação**
- Sem internet → erro amigável
- Retry funciona

---

## F05 — Resultado e Orientação

> 🟠 **Alta** • Esforço M • **Depende de:** F04

**Objetivo:** apresentar a classificação fielmente ao design, com mascote no mood adequado, chips de ação e summary card.

**Critérios de aceitação da feature:**
- Tela exibe corretamente os 3 níveis com cores do design
- Mascote no mood `calm` (LOW), `watch` (MOD) ou `alert` (HIGH)
- Chips de ação contextuais (3 itens)
- Summary card com criança, sintoma, data, score
- Botão SAMU 192 visível em HIGH

### Tasks

#### F05-T01 — Tela "Resultado da triagem" (hero + chips + summary)
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Pedro (`PedroLucasSCPB`)
- **Dependências:** F04-T15, F00-T10 (Mascote)
- **Labels:** `frontend`, `feature`

**Descrição**
Tela exibida após o questionário, recebendo o resultado retornado pelo POST de avaliações. Layout fiel a `design/peditriagem-screens-b.jsx` (linhas 173-300).

**Tarefas**
- [ ] Hero card colorido conforme nível (gradient softer → white)
- [ ] Ícone do nível (check / warn / alert) em círculo grande
- [ ] Pill com label do nível ("Baixo risco" / "Risco moderado" / "Alto risco")
- [ ] Título grande + mensagem explicativa
- [ ] Seção "O que fazer agora" com 3 chips numerados (vindos do back)
- [ ] Summary card: Criança / Sintoma principal / Data / Pontuação (X/Y)
- [ ] Disclaimer informativo no rodapé

**Critérios de aceitação**
- 3 visuais distintos para os 3 níveis
- Visual fiel ao design

---

#### F05-T02 — CTAs por nível de risco
- **Tipo:** frontend
- **Estimativa:** P
- **Sugestão de assignee:** Pedro (`PedroLucasSCPB`)
- **Dependências:** F05-T01
- **Labels:** `frontend`, `feature`

**Descrição**
Botões de ação específicos para cada classificação, conforme design.

**Tarefas**
- [ ] Botão primário (cor do nível) com `cta` retornado pelo back ("Ver cuidados em casa", "Ver cuidados até a consulta", "Ver orientações imediatas") → navega para Orientações
- [ ] **Apenas em HIGH:** botão "Ligar para SAMU 192" (`Linking.openURL("tel:192")`)
- [ ] Botão ghost "Voltar ao início"

**Critérios de aceitação**
- Botões 192 abrem discador no celular físico
- Cor do botão primário casa com nível

---

#### F05-T03 — Compartilhar resultado (opcional)
- **Tipo:** frontend
- **Estimativa:** P
- **Sugestão de assignee:** Pedro (`PedroLucasSCPB`)
- **Dependências:** F05-T01
- **Labels:** `frontend`, `enhancement`, `priority: low`

**Descrição**
Botão de compartilhar via `Share` nativo (texto: "Resultado da triagem para [nome]: [nível]. [recomendação]").

**Tarefas**
- [ ] Usar API `Share` do React Native
- [ ] Texto formatado

**Critérios de aceitação**
- Compartilha por WhatsApp e outros apps

---

## F06 — Conteúdo Educativo (Orientações + About)

> 🟡 **Média** • Esforço M • **Depende de:** F00

**Objetivo:** tela "Orientações" com 4 categorias + grid por sintoma; tela "Sobre" acessível pela aba Perfil; disclaimer in-place em vários pontos do app.

**Critérios de aceitação da feature:**
- Aba "Orientações" mostra banner + 4 cards categóricos + grid de sintomas
- Aba "Perfil" → "Sobre o aplicativo" leva para tela About
- Disclaimer em cards na home, no resultado e em about (sem modal bloqueante)

### Tasks

#### F06-T01 — Tela "Orientações"
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Andre (`Andrelbf41`)
- **Dependências:** F00-T08, F00-T09
- **Labels:** `frontend`, `feature`, `documentation`

**Descrição**
Tela acessível pela aba "Orientações" no tab bar. Layout fiel a `design/peditriagem-screens-b.jsx` (linhas 305-376).

**Tarefas**
- [ ] Banner gradient no topo "Cuidados em casa pela faixa etária"
- [ ] 4 cards categóricos: "O que observar", "Quando se preocupar", "Cuidados em casa", "Quando procurar ajuda"
- [ ] Cada card tem ícone tintado, título e descrição curta
- [ ] Grid 2 colunas com sintomas (links para conteúdo específico)
- [ ] Conteúdo estático em `src/content/orientacoes.json`
- [ ] Tap em categoria/sintoma abre tela de detalhe (`OrientacaoDetalheScreen`)

**Critérios de aceitação**
- 4 cards exibidos com cores corretas
- Tap navega para detalhe
- Visual fiel ao design

---

#### F06-T02 — Tela de detalhe da orientação
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Andre (`Andrelbf41`)
- **Dependências:** F06-T01
- **Labels:** `frontend`, `feature`, `documentation`

**Descrição**
Tela mostrada ao tocar em uma categoria ou sintoma. Conteúdo educativo de leitura.

**Tarefas**
- [ ] Layout limpo com Title + texto formatado
- [ ] Conteúdo dos 9 sintomas + 4 categorias em `src/content/orientacoes.json`
- [ ] Cada texto cita fonte (SBP, Ministério da Saúde, etc.)

**Critérios de aceitação**
- 9 sintomas + 4 categorias têm conteúdo
- Fonte citada no rodapé

---

#### F06-T03 — Tela "Sobre o aplicativo"
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Jefferson (`jeffersonEzequiel`)
- **Dependências:** F00-T08
- **Labels:** `frontend`, `feature`, `documentation`

**Descrição**
Tela acessível via aba Perfil → "Sobre o aplicativo". Layout fiel a `design/peditriagem-screens-b.jsx` (linhas 508-573).

**Tarefas**
- [ ] Hero gradient com nome "PediTriagem" + versão
- [ ] Card descritivo (apoio à decisão para pais)
- [ ] Card de aviso (fundo amarelo): "Não substitui consulta médica"
- [ ] Lista de links: Termos de uso, Política de privacidade, Fontes clínicas, Equipe e créditos
- [ ] Telas dos 4 links podem ser placeholder simples por enquanto (ou conteúdo curto)
- [ ] Footer "© 2026 · Projeto acadêmico"

**Critérios de aceitação**
- Layout fiel ao design
- Links abrem telas com conteúdo (mesmo que mínimo)

---

#### F06-T04 — Disclaimer in-place na home e no resultado
- **Tipo:** frontend
- **Estimativa:** P
- **Sugestão de assignee:** Jefferson (`jeffersonEzequiel`)
- **Dependências:** F00-T08
- **Labels:** `frontend`, `feature`

**Descrição**
Cards de disclaimer já modelados no design (home e resultado). Implementar como componente reutilizável.

**Tarefas**
- [ ] Componente `<DisclaimerCard variant="info" | "warn" />`
- [ ] Variant "info": fundo azul claro, ícone shield (usado na home)
- [ ] Variant "warn": fundo amarelo claro, ícone warn (usado em about)
- [ ] Texto pre-definido + opção de override via prop

**Critérios de aceitação**
- Componente é usado em pelo menos 3 telas (home, resultado, about)

---

## F07 — Polimento, Build e Apresentação

> 🟠 **Alta** • Esforço M • **Sprint final**

**Objetivo:** entregar um app polido, com build funcional e roteiro de demo pronto.

### Tasks

#### F07-T01 — Splash screen e ícone do app
- **Tipo:** frontend
- **Estimativa:** P
- **Sugestão de assignee:** Matheus (`jomatheusdev`)
- **Dependências:** —
- **Labels:** `frontend`, `enhancement`

**Descrição**
Configurar `app.json` do Expo com splash personalizada e ícone do app. Reaproveitar mascote / cor primária do design.

**Tarefas**
- [ ] Ícone 1024x1024 PNG (versão simplificada do mascote)
- [ ] Splash 1284x2778 com cor de fundo navy + mascote
- [ ] Ajustar `expo-splash-screen`

**Critérios de aceitação**
- Splash aparece no boot do app
- Ícone aparece na lista de apps

---

#### F07-T02 — Tema visual coerente (auditoria final)
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Matheus (`jomatheusdev`)
- **Dependências:** todas as features de tela
- **Labels:** `frontend`, `enhancement`

**Descrição**
Pass de revisão em todas as telas comparando com o protótipo `design/PediTriagem.html`.

**Tarefas**
- [ ] Auditar cada tela visualmente
- [ ] Corrigir hardcoded styles
- [ ] Validar em device real

**Critérios de aceitação**
- Sem cores nem espaçamentos hardcoded
- Visual consistente com o design

---

#### F07-T03 — Loading e erro consistentes
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Caio (`caiomps`)
- **Dependências:** F00-T08
- **Labels:** `frontend`, `enhancement`

**Descrição**
Componentes `<Loading />` e `<ErrorState />` reutilizáveis em todas as telas com chamadas de rede.

**Tarefas**
- [ ] Implementar componentes
- [ ] Refatorar telas existentes para usá-los

**Critérios de aceitação**
- Comportamento de loading e erro é igual em todas as telas

---

#### F07-T04 — Atualizar README com setup detalhado
- **Tipo:** full-stack
- **Estimativa:** P
- **Sugestão de assignee:** Ga (`ehoga`)
- **Dependências:** todas as features principais prontas
- **Labels:** `documentation`

**Descrição**
README final com pré-requisitos, comandos passo a passo, troubleshooting comum.

**Tarefas**
- [ ] Pré-requisitos: Java 21, Node 20, Expo Go no celular
- [ ] Como rodar back e front
- [ ] Como gerar APK
- [ ] Print das telas principais

**Critérios de aceitação**
- Alguém de fora do time consegue subir o projeto seguindo só o README

---

#### F07-T05 — Build iOS via EAS + publicar no TestFlight
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Matheus (`jomatheusdev`)
- **Dependências:** F07-T01, F07-T02
- **Labels:** `frontend`, `infra`

**Descrição**
Build iOS distribuível via EAS Build (Expo) e publicação no **TestFlight** para que time e banca testem em iPhones reais. EAS faz o build na cloud da Expo (free tier) — não precisa de Mac/Xcode local.

**Pré-requisitos externos:** conta Apple Developer ativa ($99/ano — alguém do time já possui). App Manager: Matheus (`jomatheusdev`).

**Tarefas**
- [ ] Confirmar bundle ID (sugestão: `com.peditriagem.app`) e nome do app no App Store Connect
- [ ] Criar conta gratuita na Expo (https://expo.dev) e rodar `eas login` + `eas init`
- [ ] Configurar `eas.json` com profile `preview` (perfeito para TestFlight)
- [ ] Configurar `app.json` com `ios.bundleIdentifier`, `ios.buildNumber`, ícones iOS específicos
- [ ] `eas credentials` para EAS gerenciar certificados e provisioning profiles automaticamente
- [ ] `eas build --platform ios --profile preview`
- [ ] `eas submit --platform ios --latest` (sobe para App Store Connect)
- [ ] No App Store Connect, configurar TestFlight:
  - [ ] Adicionar 8 devs do time como Internal Testers
  - [ ] Convidar emails da banca/professor antes da apresentação
- [ ] Documentar no README como instalar via TestFlight

**Critérios de aceitação**
- App instala via TestFlight em pelo menos 1 iPhone real do time
- App consegue se comunicar com backend local (instruir IP local na rede WiFi durante apresentação)
- Build aparece em App Store Connect e Internal Testing está habilitado

**Notas**
- TestFlight Internal Testing fica disponível em ~10 min após o build subir (sem review da Apple)
- Builds expiram em 90 dias — fazer novo build perto da apresentação

---

#### F07-T06 — Roteiro de demonstração
- **Tipo:** full-stack
- **Estimativa:** P
- **Sugestão de assignee:** Ga (`ehoga`) + Matheus (`jomatheusdev`)
- **Dependências:** todas as features principais
- **Labels:** `documentation`

**Descrição**
`docs/DEMO.md` com passo a passo da apresentação: ordem das telas, dados que serão usados, fluxo de cada nível de risco a demonstrar.

**Tarefas**
- [ ] Roteiro com tempo estimado (~10-15 min)
- [ ] Dados de teste (usuário pré-cadastrado, criança pré-cadastrada)
- [ ] Pelo menos 1 cenário verde, 1 amarelo, 1 vermelho (com red flag)
- [ ] Plano B se algo falhar (vídeo gravado em backup)

**Critérios de aceitação**
- Time ensaia uma vez seguindo o roteiro

---

#### F07-T07 (opcional) — Publicar API doc no readme.io Free
- **Tipo:** full-stack
- **Estimativa:** P
- **Sugestão de assignee:** Ga (`ehoga`)
- **Dependências:** F07-T04
- **Labels:** `documentation`, `priority: low`

**Descrição**
Se sobrar tempo, exportar `openapi.json` do Springdoc e jogar no plano Free do readme.io só para link visualmente bonito na apresentação.

**Tarefas**
- [ ] Criar conta no readme.com (free)
- [ ] Importar `openapi.json` exportado
- [ ] Customizar tema básico
- [ ] Adicionar URL no README

**Critérios de aceitação**
- Link público da doc funciona
- Não bloqueia outras tarefas se não fizer

---

## F08 — Testes Automatizados

> 🟠 **Alta (paralelo)** • Esforço M • **Transversal**

**Objetivo:** garantir que o engine (parte mais crítica) é testado e que o backend não quebra silenciosamente.

### Tasks

#### F08-T01 — Configurar JUnit 5 + Mockito + Testcontainers
- **Tipo:** backend
- **Estimativa:** M
- **Sugestão de assignee:** Saniel (`MSanielMartins`)
- **Dependências:** F00-T01
- **Labels:** `test`, `backend`, `infra`

**Descrição**
Setup de testes com Postgres real via Testcontainers — cada test run sobe um container Postgres dockerizado isolado, garantindo paridade com produção.

**Tarefas**
- [ ] Verificar dep `spring-boot-starter-test`
- [ ] Adicionar deps `org.testcontainers:junit-jupiter` e `org.testcontainers:postgresql`
- [ ] Adicionar `org.springframework.boot:spring-boot-testcontainers` (Spring Boot 3.1+)
- [ ] Criar classe abstrata `AbstractIntegrationTest` com `@Testcontainers` + container `PostgreSQLContainer`
- [ ] Configurar `@DynamicPropertySource` para apontar `spring.datasource.url` ao container
- [ ] Rodar teste do contexto da aplicação (`@SpringBootTest`)
- [ ] Documentar pré-requisito: Docker rodando na máquina

**Critérios de aceitação**
- `./mvnw test` sobe Postgres em container e roda sem erro
- Testes não dependem do Postgres do docker-compose (são isolados)

---

#### F08-T02 — Testes do TriagemService (engine de scoring + red flags)
- **Tipo:** backend
- **Estimativa:** G
- **Sugestão de assignee:** Saniel (`MSanielMartins`)
- **Dependências:** F04-T03
- **Labels:** `test`, `backend`, `priority: high`

**Descrição**
Testes unitários cobrindo os 9 sintomas com 1 caso por nível de risco + 1 caso de red flag por sintoma.

**Tarefas**
- [ ] Para cada sintoma, criar 4 testes (LOW, MOD, HIGH-via-score, HIGH-via-redflag)
- [ ] Testes específicos do algoritmo: score 2 = LOW, 3 = MOD, 5 = MOD, 6 = HIGH
- [ ] Verificar cobertura com JaCoCo

**Critérios de aceitação**
- 36 testes passando (9 sintomas × 4 cenários)
- Cobertura do `TriagemService` ≥ 80%

---

#### F08-T03 — Testes de integração dos endpoints de auth
- **Tipo:** backend
- **Estimativa:** M
- **Sugestão de assignee:** Saniel (`MSanielMartins`)
- **Dependências:** F01
- **Labels:** `test`, `backend`

**Tarefas**
- [ ] Register sucesso e duplicado
- [ ] Login sucesso e falha
- [ ] Acesso a `/api/criancas` sem token → 401
- [ ] Acesso com token válido → 200

**Critérios de aceitação**
- Cenários acima passam

---

#### F08-T04 — Testes de integração dos endpoints de criança
- **Tipo:** backend
- **Estimativa:** M
- **Sugestão de assignee:** Igor (`igorxdd`)
- **Dependências:** F02
- **Labels:** `test`, `backend`

**Tarefas**
- [ ] Criar criança, listar, atualizar, excluir
- [ ] User A não consegue acessar criança do User B (404)
- [ ] Avatar emoji inválido → 400

**Critérios de aceitação**
- Isolamento entre usuários é validado por teste

---

#### F08-T05 — Configurar Jest + RTL no frontend
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Pedro (`PedroLucasSCPB`)
- **Dependências:** F00-T06
- **Labels:** `test`, `frontend`, `infra`

**Tarefas**
- [ ] Instalar `jest-expo` e `@testing-library/react-native`
- [ ] Configurar `jest.config.js`
- [ ] Primeiro teste de smoke (renderiza tela inicial)

**Critérios de aceitação**
- `npm test` roda

---

#### F08-T06 — Smoke tests das telas principais
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Pedro (`PedroLucasSCPB`)
- **Dependências:** F08-T05
- **Labels:** `test`, `frontend`

**Tarefas**
- [ ] 1 teste por tela principal (Login, Cadastro Criança, Sintomas, Quiz, Resultado, Histórico, Perfil)
- [ ] Testar `formatAge` (helper de F02-T03)
- [ ] Mockar AuthContext e API quando necessário

**Critérios de aceitação**
- 7+ testes de renderização passando

---

## F09 — Histórico de Avaliações

> 🟠 **Alta** • Esforço M • **Depende de:** F04 (Avaliacao já é persistida) • **NOVO**

**Objetivo:** usuário consulta avaliações anteriores, filtradas por criança, com stats agregados.

**Critérios de aceitação da feature:**
- Aba "Histórico" no tab bar mostra timeline de avaliações com cor por nível de risco
- Filtro por criança (Todos / Maria / Lucas / ...)
- Stats no topo: total, baixo risco, alto risco
- Home mostra "Avaliações recentes" (últimas 2)

### Tasks

#### F09-T01 — Endpoint `GET /api/avaliacoes` (lista do usuário)
- **Tipo:** backend
- **Estimativa:** M
- **Sugestão de assignee:** Ga (`ehoga`)
- **Dependências:** F04-T05
- **Labels:** `backend`, `feature`

**Descrição**
Lista todas as avaliações das crianças do usuário, ordenadas por data desc. Paginação simples.

**Tarefas**
- [ ] Query parameters: `criancaId` (opcional), `page`, `size`
- [ ] DTO `AvaliacaoResumo` com `{id, crianca: {id, nome, avatarEmoji}, sintoma: {id, nome}, classificacao, criadoEm}`
- [ ] Filtra por usuario do JWT

**Critérios de aceitação**
- Retorna lista correta paginada
- Filtro por criança funciona

---

#### F09-T02 — Endpoint `GET /api/avaliacoes/stats`
- **Tipo:** backend
- **Estimativa:** P
- **Sugestão de assignee:** Ga (`ehoga`)
- **Dependências:** F04-T05
- **Labels:** `backend`, `feature`

**Descrição**
Stats agregados das avaliações do usuário.

**Tarefas**
- [ ] Retorna `{total, low, mod, high}`

**Critérios de aceitação**
- Counts batem com a query manual

---

#### F09-T03 — Endpoint `GET /api/avaliacoes/{id}` (detalhe)
- **Tipo:** backend
- **Estimativa:** P
- **Sugestão de assignee:** Ga (`ehoga`)
- **Dependências:** F04-T05
- **Labels:** `backend`, `feature`

**Descrição**
Retorna avaliação completa para detalhe (futuro pós-MVP, mas endpoint já facilita).

**Tarefas**
- [ ] DTO completo com classificacao, score, recomendacao, respostas
- [ ] 404 se não pertence ao usuário

**Critérios de aceitação**
- Endpoint funciona e respeita isolamento

---

#### F09-T04 — Tela "Histórico"
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Pedro (`PedroLucasSCPB`)
- **Dependências:** F09-T01, F09-T02, F00-T08
- **Labels:** `frontend`, `feature`

**Descrição**
Tela acessível pela aba "Histórico" no tab bar. Layout fiel a `design/peditriagem-screens-b.jsx` (linhas 381-417).

**Tarefas**
- [ ] Stats no topo: 3 cards (Total, Baixo risco, Alto risco)
- [ ] Tabs horizontais: Todos + uma por criança cadastrada
- [ ] Lista timeline com `<HistoryRow />` (avatar tintado por nível, nome+sintoma, data, label do risco)
- [ ] Pull-to-refresh
- [ ] Estado vazio: "Nenhuma avaliação ainda — faça a primeira!"

**Critérios de aceitação**
- Stats e lista carregam corretamente
- Tab por criança filtra a lista
- Visual fiel ao design

---

#### F09-T05 — Componente "Avaliações recentes" na home
- **Tipo:** frontend
- **Estimativa:** P
- **Sugestão de assignee:** Andre (`Andrelbf41`)
- **Dependências:** F09-T01
- **Labels:** `frontend`, `feature`

**Descrição**
Bloco na home que mostra últimas 2 avaliações + link "Ver todas" → navega para aba Histórico.

**Tarefas**
- [ ] Reutilizar componente `<HistoryRow />` (criado em F09-T04)
- [ ] Buscar `GET /api/avaliacoes?size=2`
- [ ] Estado vazio: não exibir o bloco

**Critérios de aceitação**
- Bloco aparece na home com últimas 2 avaliações
- Tap em "Ver todas" navega para Histórico

---

## F10 — Tela de Perfil

> 🟡 **Média** • Esforço P • **Depende de:** F01 e F02 • **NOVO**

**Objetivo:** central de gerenciamento do usuário e crianças, ponto de acesso para Sobre, Notificações (placeholder), Privacidade.

**Critérios de aceitação da feature:**
- Aba "Perfil" no tab bar mostra info do usuário, lista de crianças e seção de preferências
- Usuário pode adicionar/editar/excluir crianças daqui
- Link "Sobre o aplicativo" leva para tela About
- Links "Notificações" e "Privacidade" abrem telas placeholder ("Em desenvolvimento")
- Botão de logout

### Tasks

#### F10-T01 — Tela "Perfil"
- **Tipo:** frontend
- **Estimativa:** M
- **Sugestão de assignee:** Caio (`caiomps`)
- **Dependências:** F02-T02, F01-T05, F00-T08
- **Labels:** `frontend`, `feature`

**Descrição**
Tela acessível pela aba "Perfil" no tab bar. Layout fiel a `design/peditriagem-screens-b.jsx` (linhas 434-503).

**Tarefas**
- [ ] Card no topo com iniciais + nome do usuário + "X crianças cadastradas"
- [ ] Seção "Crianças": lista com avatar, nome, idade, peso → tap edita
- [ ] Botão "+ Adicionar criança"
- [ ] Seção "Preferências": Notificações, Privacidade e dados, Sobre o aplicativo
- [ ] Botão de Logout no final

**Critérios de aceitação**
- Layout fiel ao design
- Tap em criança abre edição
- Logout limpa AuthContext e volta para Login

---

#### F10-T02 — Telas placeholder (Notificações, Privacidade)
- **Tipo:** frontend
- **Estimativa:** P
- **Sugestão de assignee:** Caio (`caiomps`)
- **Dependências:** F10-T01
- **Labels:** `frontend`, `feature`

**Descrição**
Telas simples com mensagem "Em desenvolvimento" e ícone do tema.

**Tarefas**
- [ ] Componente reutilizável `<EmptyStateScreen icon title subtitle />`
- [ ] Tela "Notificações" e "Privacidade e dados" usando o componente

**Critérios de aceitação**
- Tap nos itens da seção Preferências abre as telas placeholder
- Visual coerente com o app

---

#### F10-T03 — Endpoint `PATCH /api/usuarios/me` (opcional)
- **Tipo:** backend
- **Estimativa:** P
- **Sugestão de assignee:** Saniel (`MSanielMartins`)
- **Dependências:** F01-T05
- **Labels:** `backend`, `feature`, `priority: low`

**Descrição**
Permitir atualizar nome do usuário (edição de perfil).

**Tarefas**
- [ ] DTO `UsuarioUpdateRequest` com `nome` opcional
- [ ] Endpoint atualiza só o usuário autenticado
- [ ] Retorna usuário atualizado

**Critérios de aceitação**
- Atualização funciona; bypass impossível para outro usuário

---

## Resumo de tasks por dev (sugestão)

> Sugestões iniciais. Conforme o time avança, devs livres pegam tasks abertas.

### Backend

**Ga (`ehoga`)** — tech lead back, 14 tasks
F00-T01, F00-T02, F00-T05, F00-T14, F02-T01, F02-T02, F04-T10, F04-T12, F04-T14, F07-T04, F07-T06, F09-T01, F09-T02, F09-T03

**Saniel (`MSanielMartins`)** — 14 tasks
F00-T04, F01-T01, F01-T02, F01-T03, F01-T04, F01-T05, F01-T06, F04-T08, F04-T09, F04-T13, F08-T01, F08-T02, F08-T03, F10-T03

**Igor (`igorxdd`)** — 12 tasks
F00-T03, F03-T01, F03-T02, F04-T01, F04-T02, F04-T03, F04-T04, F04-T05, F04-T06, F04-T07, F04-T11, F08-T04

### Frontend

**Matheus (`jomatheusdev`)** — tech lead front, 11 tasks
F00-T07, F00-T08, F00-T11, F00-T12, F00-T13, F00-T14, F01-T09, F01-T10, F07-T01, F07-T02, F07-T05

**Caio (`caiomps`)** — 8 tasks
F00-T06, F01-T07, F01-T08, F02-T04, F02-T05, F07-T03, F10-T01, F10-T02

**Jefferson (`jeffersonEzequiel`)** — 5 tasks
F02-T03, F04-T15, F04-T16, F06-T03, F06-T04

**Pedro (`PedroLucasSCPB`)** — 8 tasks
F00-T09, F03-T03, F05-T01, F05-T02, F05-T03, F08-T05, F08-T06, F09-T04

**Andre (`Andrelbf41`)** — 4 tasks
F00-T10, F06-T01, F06-T02, F09-T05

---

## Como abrir as tasks como issues no GitHub

Sugestão para o tech lead criar issues a partir deste documento:

1. Para cada task, criar issue com **título** = `F0X-T0Y — Título da task`
2. **Body** = copiar a seção da task (descrição, tarefas, critérios)
3. **Labels** = aplicar as labels já criadas no repositório
4. **Milestone** = vincular à milestone correta (Setup do projeto, MVP, Entrega final)
5. **Assignee** = atribuir conforme sugestão acima

> Posso scriptar essa criação automaticamente via `gh issue create` se o time preferir. Pedir ao tech lead.

---

**Última atualização:** 2026-05-04
**Autores:** Time PJI Triagem Pediátrica
