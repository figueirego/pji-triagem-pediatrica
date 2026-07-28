# Arquitetura PediTriagem — análise ponta a ponta

> **Nota de status:** este documento registra a arquitetura planejada. O contrato operacional atual do backend integrado está em [`openapi.yaml`](./openapi.yaml).

> Documento de arquitetura do sistema **PediTriagem**, escrito como sessão de mentoria.
> Cobre visão de contexto, containers, camadas internas (back e front), modelo de dados,
> engine de triagem, fluxos end-to-end, decisões críticas e insights de domínio (saúde).
> Para visão de produto e plano de execução, ver [`PLAN.md`](./PLAN.md) e [`FEATURES.md`](./FEATURES.md).

## Sumário

1. [Como pensar em arquitetura antes de desenhar caixinhas](#1-como-pensar-em-arquitetura-antes-de-desenhar-caixinhas)
2. [Visão de Contexto (C4 nível 1)](#2-visão-de-contexto-c4-nível-1)
3. [Visão de Containers (C4 nível 2)](#3-visão-de-containers-c4-nível-2)
4. [Backend — arquitetura interna em camadas](#4-backend--arquitetura-interna-em-camadas)
5. [Frontend — arquitetura interna do app](#5-frontend--arquitetura-interna-do-app)
6. [Modelo de dados (ER) e por que ele é assim](#6-modelo-de-dados-er-e-por-que-ele-é-assim)
7. [O Engine de Triagem (o coração)](#7-o-engine-de-triagem-o-coração)
8. [Fluxo end-to-end: "quero avaliar minha filha"](#8-fluxo-end-to-end-quero-avaliar-minha-filha)
9. [Cross-cutting concerns (segurança, erros, observabilidade)](#9-cross-cutting-concerns)
10. [Decisões arquiteturais — análise crítica](#10-decisões-arquiteturais--análise-crítica)
11. [Insights específicos de domínio (saúde)](#11-insights-específicos-de-domínio-saúde)
12. [O que está bom, o que é frágil, o que evoluir](#12-o-que-está-bom-o-que-é-frágil-o-que-evoluir)
13. [Observações sobre o `pom.xml` atual](#13-observações-sobre-o-pomxml-atual)

---

## 1. Como pensar em arquitetura antes de desenhar caixinhas

Antes de qualquer diagrama, um arquiteto sênior pergunta **5 coisas**:

| Pergunta | Resposta (tirada dos docs) |
|---|---|
| **Quem usa?** | Pais/responsáveis (ator único do MVP). Não há médico, não há admin. |
| **O que é crítico?** | A **classificação de risco correta**. Errar uma criança em risco alto é o pior cenário possível. |
| **O que muda rápido vs devagar?** | Telas mudam toda semana. **Protocolo clínico muda devagar**, mas QUANDO muda, muda em tudo. |
| **Onde está o risco?** | Domínio: saúde infantil → implicação legal/ética. Operacional: time de 8 devs com níveis variados em 4-6 semanas. |
| **Qual o eixo de qualidade?** | **Correção clínica > UX > performance > escalabilidade**. Não tem 100k usuários simultâneos no MVP — não otimiza para isso. |

**Por que isso importa antes do desenho?** Toda decisão arquitetural é um trade-off. Sem saber o que você está otimizando, você toma decisões pelo hype (microsserviços! Kubernetes!) em vez de pelo problema. Aqui o problema é: **time pequeno, prazo curto, domínio sensível, app único**. Isso quase grita "monolito modular bem feito". E é exatamente o que o plano descreve.

---

## 2. Visão de Contexto (C4 nível 1)

```
                          ┌────────────────────────┐
                          │                        │
         ┌──────────────► │   PediTriagem System   │ ◄──────────────┐
         │                │  (App + API + DB)      │                │
         │ usa via app    │                        │ telefona em    │
         │                └────────────────────────┘ emergência     │
         │                                                          │
┌────────┴─────────┐                                       ┌────────┴─────────┐
│  Pai/Responsável │                                       │   SAMU 192       │
│  (ator único)    │                                       │ (sistema externo │
└──────────────────┘                                       │   de emergência) │
                                                           └──────────────────┘
                          ┌────────────────────────┐
                          │  App Store Connect /   │
                          │  TestFlight (Apple)    │
                          └───────────┬────────────┘
                                      │ distribui binário iOS
                                      ▼
                          (instalado no iPhone do usuário)
```

**Insight de arquiteto:** o "sistema externo SAMU 192" parece bobo num diagrama, mas ele é importante porque representa o **limite de responsabilidade do app**. PediTriagem **não atende emergência** — ele orienta a chamar quem atende. Esse limite tem que estar claro em código (botão "Ligar 192"), em UX (chip vermelho destacado), em texto (disclaimer) e em arquitetura (engine **nunca** diz "está tudo bem, fique em casa" — ele orienta a procurar atendimento até em LOW). Arquitetura também desenha **o que o sistema NÃO faz**.

---

## 3. Visão de Containers (C4 nível 2)

```
┌──────────────────────────────┐
│         iPhone do            │
│         responsável          │
│  ┌────────────────────────┐  │            HTTPS / JSON
│  │  PediTriagem App       │  │       (REST + JWT no header)
│  │  React Native + Expo   │ ─┼────────────┐
│  │                        │  │            │
│  │  - AuthContext         │  │            │
│  │  - AsyncStorage (token)│  │            │
│  │  - axios + interceptor │  │            │
│  └────────────────────────┘  │            │
└──────────────────────────────┘            │
                                            ▼
                            ┌──────────────────────────────┐
                            │   Backend API                │
                            │   Spring Boot 3 (Java 21)    │
                            │                              │
                            │   - Spring Security + JWT    │
                            │   - REST Controllers         │
                            │   - Domain Services          │
                            │   - Triagem Engine ★         │
                            │   - JPA Repositories         │
                            │   - Springdoc OpenAPI        │
                            └──────────────┬───────────────┘
                                           │ JDBC
                                           ▼
                            ┌──────────────────────────────┐
                            │   PostgreSQL 16              │
                            │   (Docker em dev / prod      │
                            │    local na apresentação)    │
                            │                              │
                            │   - Schema: public           │
                            │   - JSONB para respostas     │
                            └──────────────────────────────┘

★ = núcleo crítico do produto (correção clínica)
```

**Por que três containers e não dois (sem DB) ou cinco (com cache, fila, etc.)?**

- **Três é o mínimo viável** para uma stack cliente-servidor com persistência: cliente, servidor, dado.
- **Não tem cache (Redis)** porque: catálogo de sintomas tem 9 itens, perguntas mudam raramente, tráfego é baixo. Cache é otimização para problema que vocês não têm. Se algum dia tiverem, JPA + `@Cacheable` em memória já resolve.
- **Não tem fila (RabbitMQ/Kafka)** porque não há processamento assíncrono. Triagem é síncrona — usuário responde, vê resultado imediato. Inserir fila aqui é **sobre-engenharia**.
- **Não tem CDN/static separado** porque o app é nativo e o conteúdo educativo está no próprio bundle (`src/content/orientacoes.json` no plano F06).

**Insight:** o melhor arquiteto não é o que adiciona componentes — é o que justifica cada um. Toda caixa no diagrama tem custo (deploy, monitoramento, debugging, onboarding). Você só ganha o direito de adicionar uma quando justifica.

---

## 4. Backend — arquitetura interna em camadas

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          BACKEND (Spring Boot 3)                        │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  Web Layer (Boundary HTTP)                                      │    │
│  │  ─────────────────────────                                      │    │
│  │  AuthController  CriancaController  SintomaController           │    │
│  │  AvaliacaoController  HealthController                          │    │
│  │  ┌──────────────────────────────────────────────────────────┐   │    │
│  │  │ Filtros: CorsFilter → JwtAuthenticationFilter → ...      │   │    │
│  │  │ DTOs request/response (com Bean Validation)              │   │    │
│  │  │ @RestControllerAdvice (tradução de exceções → ApiError)  │   │    │
│  │  └──────────────────────────────────────────────────────────┘   │    │
│  └────────────────────────────────┬────────────────────────────────┘    │
│                                   │                                     │
│  ┌────────────────────────────────▼────────────────────────────────┐    │
│  │  Application / Service Layer (lógica de negócio + orquestração) │    │
│  │  ───────────────────────────────────────────                    │    │
│  │  AuthService       (register, login, hash bcrypt, emite JWT)    │    │
│  │  CriancaService    (CRUD com filtro por usuário do JWT)         │    │
│  │  SintomaService    (catálogo)                                   │    │
│  │  TriagemService ★  (engine: scoring + red flags)                │    │
│  │  AvaliacaoService  (orquestra: triagem → persist → response)    │    │
│  │  JwtService        (gera/valida tokens)                         │    │
│  └────────────────────────────────┬────────────────────────────────┘    │
│                                   │                                     │
│  ┌────────────────────────────────▼────────────────────────────────┐    │
│  │  Domain Model (Entities JPA — POJOs com regras invariantes)     │    │
│  │  ─────────────────────────                                      │    │
│  │  Usuario   Crianca   Sintoma   Pergunta   OpcaoPergunta         │    │
│  │  PesoYesNo   Avaliacao   Classificacao (enum)                   │    │
│  └────────────────────────────────┬────────────────────────────────┘    │
│                                   │                                     │
│  ┌────────────────────────────────▼────────────────────────────────┐    │
│  │  Persistence Layer (Spring Data JPA)                            │    │
│  │  ──────────────────                                             │    │
│  │  UsuarioRepository  CriancaRepository  SintomaRepository        │    │
│  │  PerguntaRepository  AvaliacaoRepository                        │    │
│  └────────────────────────────────┬────────────────────────────────┘    │
└───────────────────────────────────┼─────────────────────────────────────┘
                                    │
                                    ▼
                              [PostgreSQL]
```

### 4.1 Por que essa estrutura em camadas?

Esse é o **layered/N-tier clássico** e ele resolve um problema real: **separar coisas que mudam por motivos diferentes**.

| Camada | Muda quando | Quem precisa entender |
|---|---|---|
| Web/Controller | Contrato HTTP muda (novo endpoint, formato JSON) | Quem integra com o app |
| Service | Regra de negócio muda | O time de produto/clínico |
| Domain | A modelagem do problema muda | Todos |
| Persistence | DB ou ORM muda | Time de backend |

Isso é o princípio de **Single Responsibility no nível arquitetural**. Cada camada tem **um motivo** para mudar. Quando você obedece isso, refatorar fica barato. Quando ignora, tudo vira espaguete.

### 4.2 O que NÃO usar nesse projeto (e por quê)

- **❌ Hexagonal Architecture / Ports & Adapters completo:** seria over-engineering. Vocês têm 1 ator, 1 adapter HTTP, 1 adapter de DB. Hexagonal brilha quando você tem múltiplos canais de entrada (HTTP + CLI + fila) e múltiplos sistemas externos. Aqui não tem.
- **❌ CQRS (Command Query Responsibility Segregation):** mesmo motivo. Seu volume de leitura ≈ volume de escrita, e os modelos de leitura e escrita são quase iguais.
- **❌ Microsserviços:** time de 8 devs por 6 semanas. Microsserviço aqui é suicídio organizacional.
- **❌ DDD pesado (agregados, eventos de domínio, repositórios em interfaces):** atrai discussão acadêmica que consome o tempo. Para o MVP, "Service + Entity JPA" é honesto e direto.

**Lição de arquiteto:** "boas práticas" sem contexto são apenas **práticas**. A arte está em saber quando aplicar.

### 4.3 Estrutura de pacotes recomendada

Há duas escolas: **layered** (`controller/`, `service/`, `repository/`) ou **package-by-feature** (`auth/`, `crianca/`, `triagem/`). Para esse tamanho de time recomendo **package-by-feature**, porque cada feature do plano (F01, F02, F04...) corresponde a um pacote, e PRs ficam isolados:

```
com.pji.triagem
├── TriagemApplication.java
├── auth/
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtService.java
│   ├── SecurityConfig.java
│   ├── dto/  (RegisterRequest, LoginRequest, AuthResponse)
│   └── Usuario.java + UsuarioRepository.java
├── crianca/
│   ├── CriancaController.java
│   ├── CriancaService.java
│   ├── Crianca.java
│   ├── CriancaRepository.java
│   └── dto/
├── sintoma/
│   ├── SintomaController.java
│   ├── SintomaService.java
│   ├── Sintoma.java + Pergunta.java + OpcaoPergunta.java + PesoYesNo.java
│   └── ...Repository.java
├── triagem/                    ★ núcleo crítico
│   ├── AvaliacaoController.java
│   ├── AvaliacaoService.java
│   ├── TriagemEngine.java      ← lógica pura, fácil de testar
│   ├── Classificacao.java (enum)
│   ├── Avaliacao.java
│   └── AvaliacaoRepository.java
├── shared/
│   ├── ApiError.java
│   ├── GlobalExceptionHandler.java
│   ├── WebConfig.java (CORS)
│   └── OpenApiConfig.java
└── health/
    └── HealthController.java
```

**Por que vale dividir o `TriagemEngine` separado do `AvaliacaoService`?** Engine é **lógica pura** (recebe respostas, retorna classificação). Service **orquestra** (carrega da DB, valida ownership da criança, chama engine, persiste). Engine pode ser testado sem subir Spring nenhum — é um POJO. Service precisa de Testcontainers porque toca DB. Essa separação é o que faz `F04-T03` (cobertura ≥ 80%) ser factível.

---

## 5. Frontend — arquitetura interna do app

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    APP MOBILE (React Native + Expo)                     │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  Navigation Layer                                               │    │
│  │  ─────────────────                                              │    │
│  │   RootNavigator                                                 │    │
│  │      ├── (não logado) → AuthStack (Login, Register)             │    │
│  │      └── (logado)     → MainTabs                                │    │
│  │                            ├── Início (Home)                    │    │
│  │                            ├── Avaliar (Symptoms→Quiz→Result)   │    │
│  │                            ├── Orientações (List→Detail)        │    │
│  │                            ├── Histórico                        │    │
│  │                            └── Perfil (→ AddChild, About)       │    │
│  └────────────────────────────────┬────────────────────────────────┘    │
│                                   │                                     │
│  ┌────────────────────────────────▼────────────────────────────────┐    │
│  │  Screens (telas, com lógica de fluxo da feature)                │    │
│  │  HomeScreen  LoginScreen  SymptomsScreen  QuizScreen            │    │
│  │  ResultScreen  HistoryScreen  ProfileScreen  ...                │    │
│  └────────────────────────────────┬────────────────────────────────┘    │
│                                   │                                     │
│  ┌────────────────────────────────▼────────────────────────────────┐    │
│  │  Componentes reutilizáveis (visual, agnósticos de negócio)      │    │
│  │  Card  Pill  PrimaryButton  GhostButton  ScreenHeader           │    │
│  │  ProgressBar  Mascot  Icon  HistoryRow  DisclaimerCard          │    │
│  └────────────────────────────────┬────────────────────────────────┘    │
│                                   │                                     │
│  ┌────────────────────────────────▼────────────────────────────────┐    │
│  │  Estado global (Context API — zero libs)                        │    │
│  │  AuthContext (user, token, login, logout, isLoading)            │    │
│  │   — outros estados ficam locais com useState/useReducer         │    │
│  └────────────────────────────────┬────────────────────────────────┘    │
│                                   │                                     │
│  ┌────────────────────────────────▼────────────────────────────────┐    │
│  │  Services (camada de I/O)                                       │    │
│  │  api.js          — instância axios + interceptors               │    │
│  │  storage.js      — wrapper AsyncStorage com chaves padronizadas │    │
│  │  authApi.js      — register/login/me                            │    │
│  │  criancasApi.js  — CRUD                                         │    │
│  │  sintomasApi.js                                                 │    │
│  │  avaliacoesApi.js                                               │    │
│  └────────────────────────────────┬────────────────────────────────┘    │
│                                   │                                     │
│  ┌────────────────────────────────▼────────────────────────────────┐    │
│  │  Theme + Utils                                                  │    │
│  │  theme/colors.js typography.js spacing.js radii.js shadows.js   │    │
│  │  utils/age.js (formatAge)                                       │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
```

### 5.1 Por que Context API e não Redux/Zustand/MobX?

Estado global aqui tem 1 dimensão: **quem é o usuário e qual o token**. Resto é local da tela (formulário, paginação do histórico, etc.) ou cacheado em memória (catálogo de sintomas).

- Redux: traz boilerplate de actions/reducers para resolver problema que você não tem.
- Zustand: ótimo, mas para **uma** variável global, é canhão para mosca.
- React Query / SWR: sentido se vocês tivessem cache complexo + sincronização. No MVP, `useEffect + axios` resolve.

**Princípio:** *escolha de tecnologia segue complexidade do problema, não tendência do mercado*.

### 5.2 Por que separar `screens/` de `components/`?

- **Componentes** são reutilizáveis e visuais. Um `<PrimaryButton>` deve funcionar em qualquer tela. Não conhece axios, não conhece AuthContext.
- **Screens** orquestram componentes + chamadas de API + navegação. São **acopladas a uma feature** e podem usar contextos.

Essa separação te dá uma garantia: **se o design mudar a cor primária, você muda 1 lugar (`theme/colors.js`)**. Se você misturar componentes com lógica de negócio, refatoração de UI vira pesadelo.

### 5.3 O fluxo de auth no app (mapa mental)

```
App boot
  │
  ▼
RootNavigator monta AuthProvider
  │
  ▼
AuthProvider → useEffect de mount:
  - lê token do AsyncStorage
  - se token, chama GET /auth/me para validar
  - seta isLoading=false
  │
  ▼
Enquanto isLoading → SplashScreen
       não logado  → AuthStack
       logado      → MainTabs
  │
  ▼
Login: AuthService.login() → recebe {user, token}
  - storage.setToken(token)
  - storage.setUser(user)
  - setUser(user)  ← Context atualiza
  - RootNavigator re-renderiza → vai para MainTabs (sem navegação imperativa)

axios interceptor:
  - request: pega token do AsyncStorage e adiciona Authorization header
  - response: se 401 → storage.clearAuth() → setUser(null) → volta para Login
```

**Insight:** observe que o app **não chama `navigation.navigate("MainTabs")` no login**. Ele atualiza o `user` no Context, e o `RootNavigator` decide qual stack mostrar. Isso é **declarativo** — a UI é função do estado. Se você inverter (login → navega imperativamente), você cria bugs do tipo "consigo voltar para tela de login depois de logado". Esse padrão é a alma da boa programação React.

---

## 6. Modelo de dados (ER) e por que ele é assim

```
┌───────────────┐         ┌───────────────┐
│   USUARIO     │ 1───N → │   CRIANCA     │
├───────────────┤         ├───────────────┤
│ id (PK)       │         │ id (PK)       │
│ nome          │         │ nome          │
│ email (UQ)    │         │ data_nasc     │
│ senha (bcrypt)│         │ peso_kg       │
│ criado_em     │         │ avatar_emoji  │
└───────────────┘         │ usuario_id FK │
                          └───────┬───────┘
                                  │ 1
                                  │
                                  N
                          ┌───────▼─────────────┐         ┌───────────────┐
                          │     AVALIACAO       │ N ── 1 → │    SINTOMA    │
                          ├─────────────────────┤         ├───────────────┤
                          │ id (PK)             │         │ id (PK)       │
                          │ crianca_id (FK)     │         │ codigo (UQ)   │
                          │ sintoma_id (FK)     │         │ nome          │
                          │ classificacao (ENUM)│         │ desc_curta    │
                          │ score (int)         │         │ icone_ref     │
                          │ red_flag_detected   │         │ cor_hex       │
                          │ respostas (JSONB) ★ │         └───────┬───────┘
                          │ criado_em           │                 │ 1
                          └─────────────────────┘                 │
                                                                  N
                                                          ┌───────▼───────┐
                                                          │   PERGUNTA    │
                                                          ├───────────────┤
                                                          │ id (PK)       │
                                                          │ sintoma_id FK │
                                                          │ codigo        │
                                                          │ texto         │
                                                          │ sub           │
                                                          │ tipo (ENUM)   │ ← OPTIONS | YESNO
                                                          │ ordem         │
                                                          └───┬───────┬───┘
                                                              │       │
                                       (se tipo=OPTIONS)      │       │  (se tipo=YESNO)
                                                              ▼       ▼
                                                  ┌──────────────┐ ┌──────────────┐
                                                  │ OPCAO_PERG   │ │ PESO_YESNO   │
                                                  ├──────────────┤ ├──────────────┤
                                                  │ id (PK)      │ │ id (PK)      │
                                                  │ pergunta_id  │ │ pergunta_id  │
                                                  │ codigo       │ │ score_yes    │
                                                  │ texto        │ │ score_no     │
                                                  │ score        │ │ score_dunno  │
                                                  │ red_flag     │ │ rf_on_yes    │
                                                  └──────────────┘ │ rf_on_no     │
                                                                   └──────────────┘
```

### 6.1 Decisões interessantes nesse modelo

**🎯 Por que `Avaliacao.respostas` é JSONB?**

Imagine que vocês modelassem `RespostaAvaliacao` como tabela:

```
RESPOSTA_AVALIACAO (avaliacao_id, pergunta_codigo, valor_dado)
```

Vantagens: queries SQL podem agregar respostas por pergunta.
Desvantagens: 5 perguntas por avaliação × 1000 avaliações = 5000 linhas de detail. Cada leitura de avaliação vira `JOIN` ou `N+1`. Para o MVP, vocês **nunca consultam por resposta individual** — só salvam/exibem. JSONB resolve em uma coluna, com índice GIN se um dia precisarem.

**Trade-off honesto:** se um dia o produto evoluir para "quantas crianças responderam SIM para 'tem febre alta?'", JSONB precisa de query mais complexa que tabela. Na hora que isso virar requisito, refatora. **YAGNI** (You Aren't Gonna Need It) é melhor que sobre-engenharia preventiva.

**🎯 Por que separar `OpcaoPergunta` e `PesoYesNo` em tabelas distintas?**

Você poderia ter uma única tabela `Resposta` com colunas opcionais. Mas duas tabelas:
1. **Refletem o domínio** — pergunta YESNO tem semântica diferente de OPTIONS.
2. **Evitam nulls** — `OpcaoPergunta` tem score sempre; `PesoYesNo` tem três scores. Misturar dá NULL espaço onde não deveria existir.
3. **Validam no schema** — DB força que YESNO sempre tem 3 scores, OPTIONS sempre tem N opções.

Isso é o princípio de "**Make illegal states unrepresentable**" — quando o **schema** previne dados malformados, você não precisa validar isso no código.

**🎯 Por que `Sintoma.codigo` (slug) e não só id numérico?**

`codigo = "fever"` é estável entre ambientes (dev, prod, demo). Se vocês precisarem hardcodar "se sintoma é febre, faz X", usam `"fever"` em vez de `id=3`. **IDs são detalhes de implementação; códigos são parte do contrato**.

**🎯 Por que `dataNascimento` em vez de `idade`?**

Idade é função do tempo. Se você guarda `idade=2`, daqui a um ano você está errado. Guarde fatos invariantes (data de nascimento) e calcule derivados (idade) sob demanda. **Invariantes pertencem ao banco; derivados pertencem ao código.**

**🎯 O que está faltando no modelo (nível MVP+)?**

- **Versionamento do protocolo:** quando vocês mudarem perguntas/pesos do sintoma "febre" daqui a 6 meses, todas as avaliações antigas vão ficar com `score` referente ao protocolo antigo. Para auditoria, deveria existir `Avaliacao.protocolo_versao`. Para MVP, OK ignorar — só deixar registrado no plano de evolução.
- **Soft delete em Crianca:** se o usuário deletar uma criança que tem avaliações no histórico, FK `crianca_id` quebra o histórico ou cascateia a exclusão. Cascade está no plano (F02-T01). Para MVP é OK; em produção real eu colocaria `deleted_at` em vez de DELETE.

---

## 7. O Engine de Triagem (o coração)

Essa é a peça mais importante de toda a arquitetura.

```
                   ┌─────────────────────────────────────────┐
                   │          AvaliacaoService               │
                   │  (orquestrador, sabe de DB e segurança) │
                   └────────────────┬────────────────────────┘
                                    │
   1. valida ownership da criança   │
   2. carrega Sintoma + Perguntas   │
   3. invoca:                       │
                                    ▼
                   ┌─────────────────────────────────────────┐
                   │            TriagemEngine                │
                   │       (lógica pura, sem dependências)   │
                   ├─────────────────────────────────────────┤
                   │  classify(sintoma, perguntas, respostas)│
                   │      ↓                                  │
                   │  para cada resposta:                    │
                   │    - busca opção/peso correspondente    │
                   │    - se redFlag → marca flag            │
                   │    - acumula score                      │
                   │      ↓                                  │
                   │  if redFlagDetected   → HIGH            │
                   │  elif score < 3       → LOW             │
                   │  elif score 3..5      → MOD             │
                   │  else                 → HIGH            │
                   │      ↓                                  │
                   │  retorna Resultado(                     │
                   │    classificacao,                       │
                   │    score,                               │
                   │    redFlagDetected,                     │
                   │    recomendacao,  // por nível          │
                   │    chips[]        // 3 ações por nível  │
                   │  )                                      │
                   └────────────────┬────────────────────────┘
                                    │
   4. monta Avaliacao (entity)      │
   5. persist via repository        │
   6. retorna response DTO          ▼
                            (volta para Controller)
```

### 7.1 Por que essa separação Service vs Engine?

| Aspecto | AvaliacaoService | TriagemEngine |
|---|---|---|
| Conhece banco? | Sim (via Repository) | **Não** |
| Conhece segurança? | Sim (filtra por usuário) | **Não** |
| Conhece HTTP? | Não | Não |
| Conhece Spring? | Sim (`@Service`, transações) | **Não** (POJO puro) |
| Como testa? | Testcontainers + Postgres real | JUnit puro, em milissegundos |
| Cobertura desejada | ~70% | **~95%** (é onde o erro mata) |

Engine puro é uma **função matemática** disfarçada de classe Java: dadas as mesmas entradas, retorna a mesma saída. Isso é facílimo de testar, e onde **mais erros teriam impacto humano** ele tem **mais testes**. Esse é o truque arquitetural mais importante do projeto inteiro.

### 7.2 Por que red flag tem prioridade absoluta?

O design é **conservador por construção**: na dúvida, escala. Em medicina (e em qualquer engine de risco), o pior erro é o **falso negativo** — dizer que está tudo bem quando não está. Red flag override força:

> "se qualquer resposta clínica indica risco grave, eu **ignoro** o resultado da soma de pontos."

Isso protege contra a fraqueza do scoring puro: somatórios podem mascarar um único sinal grave entre vários sinais leves. **O design clínico está embutido na arquitetura do engine**, não na UI. Isso é maturidade.

### 7.3 Por que o engine tem que ficar no servidor?

Tentação: "vamos colocar a lógica no app pra funcionar offline e ser mais rápido". **NÃO.** Razões:

1. **Atualização do protocolo:** quando a SBP mudar uma recomendação, você atualiza o servidor e **todos os usuários** ganham. Se estiver no app, depende de update na App Store + usuário aceitar update.
2. **Auditabilidade:** você precisa saber qual versão do engine classificou cada avaliação. Servidor controla isso. Cliente, não.
3. **Anti-tampering:** alguém poderia alterar a lógica no app (especialmente Android, mas iOS também jailbroken). Em domínio de saúde isso pode ter consequências sérias.
4. **Confiança no dado:** se o app calcula e só envia "HIGH" para o servidor, o servidor não pode validar. Servidor recebe **respostas** e **calcula** — ele é a fonte de verdade.

Para o MVP em apresentação local, parece overkill. Mas é a decisão correta **mesmo agora**, porque mover o engine depois é caríssimo.

---

## 8. Fluxo end-to-end: "quero avaliar minha filha"

```
Pai           App (RN)              API (Spring)            DB (Postgres)
 │               │                       │                       │
 │ abre app      │                       │                       │
 ├──────────────►│                       │                       │
 │               │ bootstrap:            │                       │
 │               │ lê token do storage   │                       │
 │               │                       │                       │
 │               │ GET /auth/me          │                       │
 │               │ (Authorization: Bearer xxx)                   │
 │               ├──────────────────────►│                       │
 │               │                       │ valida JWT (filtro)   │
 │               │                       │ carrega Usuario       │
 │               │                       ├──────────────────────►│
 │               │                       │◄──────────────────────┤
 │               │◄──────────────────────┤ 200 {id, nome, email} │
 │               │                       │                       │
 │ vai p/ Avaliar│                       │                       │
 ├──────────────►│ tela "selecionar criança"                     │
 │               │ GET /api/criancas     │                       │
 │               ├──────────────────────►│                       │
 │               │                       │ filtra por user.id    │
 │               │                       ├──────────────────────►│
 │               │                       │◄──────────────────────┤
 │               │◄──────────────────────┤ 200 [{...}, ...]      │
 │               │                       │                       │
 │ escolhe Maria │                       │                       │
 ├──────────────►│ tela "selecionar sintoma"                     │
 │               │ GET /api/sintomas     │                       │
 │               ├──────────────────────►│                       │
 │               │◄──────────────────────┤ 200 [9 sintomas]      │
 │               │                       │                       │
 │ escolhe Febre │                       │                       │
 ├──────────────►│ GET /api/sintomas/{id}/perguntas              │
 │               ├──────────────────────►│                       │
 │               │                       │ ⚠ NÃO retorna scores  │
 │               │                       │   nem red flags       │
 │               │◄──────────────────────┤ 200 [perguntas]       │
 │               │                       │                       │
 │ responde Q1   │ acumula em estado     │                       │
 ├──────────────►│ local (não envia)     │                       │
 │ ...           │                       │                       │
 │ responde Qn   │                       │                       │
 ├──────────────►│                       │                       │
 │               │ POST /api/avaliacoes                          │
 │               │ {criancaId, sintomaId, respostas[]}           │
 │               ├──────────────────────►│                       │
 │               │                       │ 1. JWT → Usuario      │
 │               │                       │ 2. valida ownership   │
 │               │                       │    da criança         │
 │               │                       ├──────────────────────►│
 │               │                       │◄──────────────────────┤
 │               │                       │ 3. carrega Sintoma+   │
 │               │                       │    Perguntas+Opcoes   │
 │               │                       ├──────────────────────►│
 │               │                       │◄──────────────────────┤
 │               │                       │ 4. TriagemEngine      │
 │               │                       │    .classify(...)     │
 │               │                       │    → resultado        │
 │               │                       │ 5. INSERT Avaliacao   │
 │               │                       ├──────────────────────►│
 │               │                       │◄──────────────────────┤
 │               │◄──────────────────────┤ 201 {id, classifica-  │
 │               │                       │   cao, recomendacao,  │
 │               │                       │   chips, ...}         │
 │               │                       │                       │
 │ vê resultado  │ navega ResultScreen   │                       │
 │◄──────────────┤                       │                       │
```

### 8.1 Coisas a aprender desse fluxo

**🎯 As respostas só vão para o servidor uma vez, no final.** O quiz inteiro é estado local da tela. Isso reduz round-trips, é mais rápido, e funciona até em conexão ruim. Trade-off: se o app crashar no meio, perde o progresso. Para MVP é OK.

**🎯 O endpoint `GET /api/sintomas/{id}/perguntas` NÃO retorna scores nem red flags.** Olha o detalhe em `F04-T04`:

> Não expor redFlag nem score no response (front não precisa saber — engine faz no back)

Isso é **defensivo**. Por que? Se o front soubesse os pesos, alguém com curiosidade (DevTools no Chrome dev build, Charles Proxy num iPhone) poderia inferir como manipular respostas. Não é segredo nuclear, mas é **bom princípio de design de API**: exponha o mínimo necessário. Princípio de **need-to-know**.

**🎯 Validação dupla de ownership** (passo 2): mesmo que o JWT esteja correto, o servidor verifica que a criança pertence àquele usuário. Por quê? Porque um atacante pode forjar `{criancaId: 7}` mesmo logado como user A, esperando avaliar criança do user B. Servidor **nunca confia em dados do cliente**. **Sempre** revalida autorização recurso a recurso.

**🎯 Erro voltando como 404 e não 403** quando criança é de outro user (`F04-T05`):
> Tentativa de avaliar criança de outro user → 404

Por quê? Para **não vazar a existência** do recurso. Se você responder 403 ("acesso negado"), o atacante sabe que a criança 7 existe. 404 ("não encontrado") esconde essa informação. Princípio de **information hiding**.

---

## 9. Cross-cutting concerns

São coisas que cortam **todas as features** e merecem ser pensadas como infra arquitetural, não como feature.

### 9.1 Segurança

```
[Request HTTP] → CORS Filter → JWT Auth Filter → Spring Security
                                       │              │
                                       ▼              ▼
                                Lê header     Decide se rota
                                Authorization é pública ou
                                Valida token  autenticada
                                Carrega user  Popula Authentication
                                              no SecurityContext
```

| Camada | Garantia |
|---|---|
| HTTPS (na entrega) | Token não vaza no fio |
| JWT assinado | Servidor confia que o token foi emitido por ele |
| Bcrypt | Senha no DB não é reversível |
| Filtro de ownership por recurso | User não acessa dados de outro |
| 404 em vez de 403 | Não vaza existência |
| Bean Validation nos DTOs | Input é sanitizado |
| Stateless (sem sessão) | Servidor escala horizontalmente trivialmente |

**Buraco confessado:** sem refresh token, expira em 24h e usuário precisa logar de novo. Para MVP de apresentação acadêmica é OK, mas vale anotar para v2.

**Buraco que eu adicionaria:** rate limiting no `/auth/login` (3 tentativas por minuto por IP) para evitar brute force. 30 minutos de trabalho com `bucket4j`.

### 9.2 Tratamento de erros

A escolha de ter um `@RestControllerAdvice` global (`F01-T06`) é fundamental. Sem isso, cada controller trata seus próprios erros e o cliente recebe respostas inconsistentes. Padrão proposto:

```json
{
  "status": 400,
  "mensagem": "Erro de validação",
  "campos": [
    { "campo": "email", "erro": "deve ser um email válido" },
    { "campo": "senha", "erro": "tamanho deve estar entre 6 e 100" }
  ]
}
```

Esse formato de `campos[]` é **um contrato com o front**. Permite o app marcar campos individualmente. **Esse pequeno detalhe melhora a UX em 10x** comparado a "Erro: bad request".

### 9.3 Observabilidade (o esquecido em MVPs)

O plano não menciona logs estruturados nem métricas. Para apresentação acadêmica é OK. Mas a versão sênior dessa arquitetura colocaria:

- **Logback config** com JSON output (timestamp, level, traceId, message, mdc)
- **Spring Boot Actuator** habilitado (`/actuator/health`, `/actuator/metrics`)
- **MDC com requestId** para correlacionar logs de uma mesma requisição

30 minutos de trabalho que salva 4 horas de debugging.

### 9.4 Documentação como contrato

`F00-T14` define que **antes** de back e front se integrarem, o contrato OpenAPI tem que existir. Isso é o maior insight organizacional do projeto. Sem isso, dois times trabalham em paralelo, integram na sexta-feira e descobrem que não conversam.

**Padrão:** *contract-first development*. Em produtos profissionais, isso é diferença entre time produtivo e time bombeiro.

---

## 10. Decisões arquiteturais — análise crítica

| # | Decisão | Veredicto | Razão |
|---|---|---|---|
| 1 | Engine **híbrido** scoring + red flags | ✅ Excelente | Captura sutilezas (somatório) sem perder gravidade (override). Modelo certo para o domínio. |
| 2 | Engine no **servidor** | ✅ Crítico | Já justifiquei na §7.3. |
| 3 | JWT stateless + bcrypt | ✅ Padrão | Padrão da indústria. Sem sessão = sem sticky-session. |
| 4 | **Postgres** em dev e prod (sem H2) | ✅ Madura | Elimina classe inteira de bugs do tipo "funciona em dev, quebra em prod". |
| 5 | **Testcontainers** | ✅ Ótima | Mesma justificativa do #4 — paridade real. Tem custo (test mais lento), mas vale. |
| 6 | **Springdoc** vs readme.io | ✅ Pragmática | Documentação cara só compensa quando você tem público externo. Vocês não têm. |
| 7 | **JSONB** para respostas | ⚠ Aceitável | Já discutida. Trade-off honesto, ressalva documentada. |
| 8 | **iOS only** via TestFlight (sem Android) | ⚠ Trade-off de escopo | Reduz cobertura mas elimina classes de problema (assinatura Android, distribuição APK, fragmentação). Para acadêmico, OK. |
| 9 | Sem refresh token | ⚠ Aceitável p/ MVP | Bate logout em 24h, mas ninguém morre. |
| 10 | Sem versionamento de API (`/v1/`) | ⚠ Aceitável p/ MVP | Adicionar `/api/v1/` agora custa zero. Adicionar depois custa muito. **Eu adicionaria já**. |
| 11 | `dataNascimento` (não `idade`) | ✅ Correta | Já justifiquei na §6. |
| 12 | Hospedagem **local** na apresentação | ✅ Pragmática | Cloud para apresentação acadêmica adiciona complexidade que não agrega valor. Backlog para depois. |
| 13 | Sem push notification | ✅ Correta no MVP | Notificação push exige fluxo Apple Developer + APNs + backend para envio. Trabalho desproporcional. |
| 14 | Cascade DELETE de Crianca→Avaliacao | ⚠ Discordo | Em saúde, **histórico é evidência**. Eu usaria soft delete. Mas para MVP acadêmico é OK. |
| 15 | Sem auditoria de quem viu o quê | ✅ OK p/ MVP | Sem perfil "médico", não há segregação de dados. |

---

## 11. Insights específicos de domínio (saúde)

Essa parte é onde a cobrança como "arquiteto sênior em saúde" aparece. O projeto acertou em quase tudo. Pontos críticos a destacar:

### 11.1 O disclaimer não é detalhe — é arquitetura

Olhem onde o disclaimer aparece (`F06-T04`): home, resultado, about. Isso é **defesa em profundidade**. O argumento legal "o usuário foi alertado" só vale se o alerta esteja em vários momentos. Eu adicionaria também:

- Toast pequeno na primeira inicialização
- Linha discreta no rodapé do quiz ("Suas respostas não substituem avaliação médica")

Não é UX ruim — é **defesa institucional**.

### 11.2 LGPD — você está coletando dados sensíveis de menores

Lei 13.709/2018 (LGPD) artigo 14: dados de crianças e adolescentes exigem consentimento específico, no melhor interesse do titular, com prevalência da palavra do responsável.

Para MVP acadêmico vocês estão protegidos. Para um produto comercial:

- Política de privacidade clara (já planejada em F06-T03)
- Coleta mínima (vocês coletam só nome, data de nascimento, peso opcional, avatar — bom)
- Direito ao esquecimento (DELETE da criança implementado, ✅)
- Tempo de retenção (não está definido — eu colocaria 24 meses sem login)
- DPO (Encarregado) nomeado

Para **agora**, eu adicionaria uma única coisa: **avisar no cadastro** que os dados ficam apenas em servidor próprio e que o usuário pode excluí-los. Esse parágrafo se chama "consentimento específico" na LGPD e custa 5 minutos para escrever.

### 11.3 Auditabilidade do diagnóstico

Imagine: daqui a 3 anos um pai diz "o app me orientou a ficar em casa e meu filho passou mal". Você precisa **provar** o que o app respondeu. Por isso `Avaliacao` persiste:

- `respostas` (o que o usuário disse)
- `classificacao` (o que o engine concluiu)
- `score` e `redFlagDetected` (por que concluiu)
- `criadoEm` (quando)

Falta uma coisa: **versão do protocolo**. Se você atualizar o quiz de febre daqui a 6 meses, e a auditoria for pedida em uma avaliação antiga, o `score=4` referente ao protocolo antigo pode parecer errado. Adicionaria `Avaliacao.protocoloVersao` (string ou int).

Custo: 1 coluna. Benefício: rastreabilidade jurídica. **Faria mesmo no MVP.**

### 11.4 Por que o time clínico tem que aprovar o quiz

`F04-T06` a `F04-T14` dizem "pesquisar protocolo simplificado". Atenção: vocês são acadêmicos, não médicos. **O quiz deve ter fonte citada (SBP, MS) e — idealmente — review de um pediatra**. Para defesa do TCC isso é um diferencial. Para evitar problema legal, é necessário.

Se não tiverem médico no time, pelo menos:
- Cite a fonte de cada quiz no commit
- Coloque aviso "baseado em protocolo público SBP / MS" na tela de orientações
- Versão zero é "apoio à decisão", **não é diagnóstico**

### 11.5 Por que orientação em LOW também sugere acompanhamento

Olha o design do mascote `calm` em `LOW`. Mesmo no melhor cenário, o app diz "observação domiciliar **com orientações**", não "está tudo bem". Isso é o tom certo. Em saúde, **nunca dê tranquilidade absoluta**. Sempre tenha "se piorar, procure atendimento". Esse tom precisa estar no copywriting do `chips[]` retornado pelo back.

---

## 12. O que está bom, o que é frágil, o que evoluir

### Está bom (mantém)

- Separação clara de containers (app / API / DB)
- Engine separado de serviço, testável puramente
- JWT stateless
- Postgres + Testcontainers (paridade dev/prod)
- Decisão de tecnologia conservadora (boring tech wins)
- Plano F00 com tudo de infra antes — destrava paralelismo
- Contract-first com Swagger/OpenAPI

### É frágil (atenção)

- **Sem versionamento da API** (`/api/...` em vez de `/api/v1/...`). Trivial agora, custa caro depois. Eu colocaria.
- **Sem versionamento do protocolo de triagem** (já discutido). Eu colocaria.
- **Sem rate limiting** em `/auth/**`. Brute force é trivial sem isso.
- **Sem refresh token**: aceitável, mas anote.
- **Sem idempotency-key** no POST /avaliacoes. Se o app perder conexão e retentar, cria avaliação duplicada. Solução: header `Idempotency-Key: <uuid>` no front. 1 dia de trabalho.
- **Cascade DELETE** em Crianca→Avaliacao destrói histórico clínico. Soft delete seria mais correto.
- **Logs e métricas** ausentes no plano. Sem isso, debugar a apresentação ao vivo será dolorido.

### Para evoluir pós-MVP

- Modo offline com sync (já no backlog do PLAN §11)
- Push notification "lembrete de retorno" caso resultado MOD/HIGH
- Modo "profissional" com login separado para enfermeiro/pediatra revisar avaliações
- Multi-tenant (escola, posto de saúde, hospital) — mas isso vira outro produto
- LLM como assistente para chat livre — só **depois** de garantir guardrails de não-diagnóstico

---

## 13. Observações sobre o `pom.xml` atual

Algumas coisas que **não batem** com o plano e precisam ser arrumadas antes de F00 começar:

1. **Spring Boot 4.0.6** (linha 8) — essa versão pode não estar estável. O plano (`PLAN §3`) diz "Spring Boot 3". Usar `3.4.x` ou `3.5.x`.
2. **`spring-boot-h2console`** (linha 36) — esse artefato **não existe**. Provavelmente foi gerado por algum tool que entendeu errado. Para H2 use `com.h2database:h2` runtime, mas como o plano fala em **remover H2**, **deletar a dependência inteira**.
3. **`spring-boot-starter-webmvc`** (linha 47) — o nome correto é `spring-boot-starter-web`.
4. **Starters de teste inexistentes** (`-data-jpa-test`, `-validation-test`, `-webmvc-test`, linhas 67-80) — o correto é `spring-boot-starter-test` (singular, cobre tudo).
5. **Falta** dependência do **PostgreSQL driver**, **Spring Security**, **JJWT**, **Springdoc**, **Testcontainers**.

Esse `pom.xml` não vai compilar como está. Isso vai ser pego em `F00-T01` quando alguém rodar `./mvnw spring-boot:run`. Não é crítico agora — só sinal de que o esqueleto foi gerado de forma não confiável e precisa ser revisado por inteiro na primeira task.

---

## Onde começar se você for um dos tech leads

**Se você for Ga (tech lead back) na próxima 2ª feira:**

1. Arrumar o `pom.xml` (ver §13). 1h.
2. Criar `docker-compose.yml` com Postgres 16. 1h.
3. Criar pacotes vazios de `auth/`, `crianca/`, `sintoma/`, `triagem/`, `shared/`, `health/`. 30min. Isso "trava" a estrutura para os outros não inventarem.
4. Subir `HealthController` mínimo + Springdoc + CORS. 2h. Já dá para o front consumir.
5. Stub de `/auth/register` retornando 501 — mas com DTO + validação correta. 1h. Define o **contrato**.
6. Postar no Slack/Discord do time: "back tem health + estrutura. Front, pode começar a chamar."

Em **meio dia** o front está desbloqueado. Esse é o ritmo de um tech lead bom: **destrava todo mundo antes de fazer suas próprias features**.

**Se você for Matheus (tech lead front):**

1. Setup tokens + Mascote + 3 componentes-chave (PrimaryButton, Card, ScreenHeader). 1 dia.
2. Telas placeholder com tab bar funcionando + AuthContext + axios. 1 dia.
3. Postar: "front tem navegação + estilo. Quem for fazer Login pode começar."

---

## Fechamento

A arquitetura aqui é **sólida, conservadora e bem-pensada**. Nenhum hype desnecessário. As decisões refletem maturidade: ferramenta certa para o problema certo, no prazo dado, com o time dado. Os pontos de fragilidade apontados (versionamento, rate limiting, idempotência, soft delete, observabilidade) são **opcionais** para MVP e **obrigatórios** para produto comercial — separação que o time já faz com o "Backlog pós-MVP" (§11 do PLAN).

Se houver **um único conselho** de arquitetura para se levar daqui: **não deixem que o engine vire código difícil de testar**. Mantenham `TriagemEngine` como uma classe Java pura, sem Spring, sem JPA, sem nada. Tudo o resto pode degradar com a pressa do prazo — esse não pode. É onde a vida da criança está.

---

**Última atualização:** 2026-05-06
**Autor:** Análise arquitetural sênior (mentoria)
