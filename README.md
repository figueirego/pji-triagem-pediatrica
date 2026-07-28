# PJI - Triagem Pediátrica

Projeto Integrador para sistema de **triagem pediátrica**, contendo frontend mobile e backend em monorepo.

## Stack

- **Frontend:** React Native com [Expo](https://expo.dev/)
- **Backend:** Java 21 + Spring Boot 3 (Maven)
- **Banco:** PostgreSQL + Flyway

## Estrutura do projeto

```
pji-triagem-pediatrica/
├── frontend/          # App React Native (Expo)
├── backend/           # API Spring Boot
├── .gitignore
├── README.md
└── CONTRIBUTING.md    # Regras de contribuição (PRs, commits, branches)
```

## Pré-requisitos

| Ferramenta | Versão | Uso |
|---|---|---|
| Node.js | 20+ | Frontend e CI |
| npm ou yarn | — | Frontend |
| Java JDK | 21 | Backend |
| Docker | — | Postgres local do backend |
| Expo Go (app) | — | Testar no celular sem build nativa |

## Como rodar

### Backend
```bash
cd backend
docker compose up -d postgres
export APP_JWT_SECRET="$(openssl rand -base64 64)"
./mvnw spring-boot:run
```
A API sobe em `http://localhost:8080`. O schema é aplicado pelo Flyway no PostgreSQL local.

Para rodar backend + banco em containers:

```bash
cd backend
cp .env.example .env
# edite APP_JWT_SECRET em .env antes de subir
docker compose up --build
```

### Frontend
```bash
cd frontend
npm install     # apenas na primeira vez
npm start
```
Após iniciar, escaneie o QR code com o app **Expo Go** ou pressione `w` para abrir no navegador.

Variáveis úteis:

```bash
EXPO_PUBLIC_API_URL=http://<IP-DA-SUA-MAQUINA>:8080
EXPO_PUBLIC_USE_MOCK_AUTH=true # opcional, apenas para demo sem backend
```

Use o IP local da máquina quando o app estiver em um celular físico na mesma rede Wi-Fi.
O login real usa CPF (`login`) e senha cadastrados em `POST /auth/register/user`.

## Qualidade do frontend

```bash
cd frontend
npm run doctor
```

O workflow `.github/workflows/frontend.yml` executa `npm ci` e `npm run doctor` em PRs que alteram o frontend.

## Contrato da API

O contrato atual do backend está em [`docs/openapi.yaml`](./docs/openapi.yaml). O frontend consome os endpoints reais de autenticação, crianças, sintomas, questionário, avaliação, orientações e histórico.

## Build Android APK

Para gerar um APK de demonstração pelo EAS:

```bash
cd frontend
npx --yes eas-cli login
npx --yes eas-cli build:configure
EXPO_PUBLIC_API_URL=http://<IP-DA-SUA-MAQUINA>:8080 npx --yes eas-cli build --platform android --profile preview
```

Ao final do build, baixe o artefato `.apk` exibido pelo EAS e instale em um Android de teste. Para apresentação local, mantenha o backend rodando e use `EXPO_PUBLIC_API_URL` com o IP da máquina na mesma rede Wi-Fi.

## Build iOS e TestFlight

O projeto já contém `frontend/eas.json` com profile `preview` para build iOS distribuível via TestFlight.

Pré-requisitos externos:

- Conta Expo autenticada com `npx eas-cli login`.
- Projeto vinculado com `npx eas-cli init`.
- Apple Developer Program ativo.
- Bundle ID acordado: `com.peditriagem.app`.
- App criado no App Store Connect.

Comandos:

```bash
cd frontend
EXPO_PUBLIC_API_URL=http://<IP-DA-SUA-MAQUINA>:8080 npm run build:ios:preview
npm run submit:ios:latest
```

Após o envio, configure os Internal Testers no App Store Connect e compartilhe o convite do TestFlight com o time e a banca. O backend continua rodando localmente na máquina da apresentação.

## Demonstração

O roteiro de apresentação está em [`docs/DEMO.md`](./docs/DEMO.md), com cenários de baixo risco, risco moderado e alto risco com red flag.

## Prints das telas principais

Os prints usados na apresentação devem ficar em `docs/screenshots/` com estes nomes:

| Tela | Arquivo sugerido |
|---|---|
| Login | `docs/screenshots/01-login.png` |
| Home | `docs/screenshots/02-home.png` |
| Seleção de sintomas | `docs/screenshots/03-sintomas.png` |
| Questionário | `docs/screenshots/04-questionario.png` |
| Resultado | `docs/screenshots/05-resultado.png` |
| Histórico | `docs/screenshots/06-historico.png` |
| Perfil | `docs/screenshots/07-perfil.png` |

Para capturar rapidamente, rode `npm start`, abra o app no Expo Go ou emulador, percorra o roteiro de [`docs/DEMO.md`](./docs/DEMO.md) e salve cada tela com os nomes acima.

## Fluxo de trabalho (importante)

- **Branches protegidas:** `main` e `dev` — nenhum push direto, **apenas via Pull Request**.
- **`main`** representa código pronto para release.
- **`dev`** é a branch de integração — todo trabalho novo é mergeado aqui primeiro.
- Cada feature/fix sai de `dev` em uma branch própria (`feature/...`, `fix/...`, `chore/...`).
- PRs exigem **1 aprovação** antes do merge.

Veja [CONTRIBUTING.md](./CONTRIBUTING.md) para detalhes do fluxo, padrões de commit e de PR.
