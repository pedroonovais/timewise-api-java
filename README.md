# TimeWise API Java

**TimeWise API Java** é uma API desenvolvida em **Spring Boot** para o sistema de **Assistente Inteligente de Produtividade Saudável**, com foco no controle de atividades de trabalho e pausas, cálculo automático de scores diários de produtividade e gerenciamento de tarefas.

A solução é modularizada em camadas e utiliza **JPA/Hibernate com PostgreSQL** rodando em **Docker Compose**. Conta também com **RabbitMQ** para processamento assíncrono de eventos e documentação interativa via **Swagger**.

---

## 📌 Funcionalidades

- 🔐 **Autenticação JWT** com registro e login de usuários
- 🔒 **Segurança** com hash BCrypt e proteção de rotas
- 📊 **Cálculo automático de Score Diário** baseado em atividades de trabalho e pausas
- 🎯 **Gerenciamento de Tarefas** com controle de status e prioridades
- ⏱️ **Gerenciamento de Atividades** (Trabalho e Pausa) com registro de tempo
- 📈 **Sistema de Mensageria Assíncrona** com RabbitMQ para cálculo de scores
- 💾 **Cache** para otimização de consultas frequentes
- 🌱 **DDL Automático** - Schema criado/atualizado automaticamente conforme entidades
- Gerenciamento de **usuários** com validações
- API RESTful com respostas em JSON e paginação
- Documentação interativa via Swagger
- Banco de dados PostgreSQL em container
- RabbitMQ em container para processamento assíncrono

---

# 👩‍💻 Participantes

- Pedro Henrique Mendonça de Novais - RM555276
- Letícia Zago de Souza - RM558464
- Ana Carolina dos Reis Santana - RM556219

---

## 🏗 Estrutura do Projeto

- **controller**: Camada de apresentação (controllers REST, endpoints)
- **service**: Camada de regras de negócio (serviços e lógica da aplicação)
- **repository**: Camada de acesso a dados (JPA repositories)
- **model**: Camada de domínio (entidades e modelos do sistema)
- **dto**: Objetos de transferência de dados (request/response)
- **mapper**: Conversores entre entidades e DTOs
- **config**: Configurações (Security, RabbitMQ, Cache, etc.)
- **auth**: Componentes de autenticação (JWT, UserDetailsService)
- **messaging**: Sistema de mensageria (publishers, listeners, eventos)
- **enums**: Enumerações do sistema

---

## 💻 Tecnologias Utilizadas

- Java 17
- Spring Boot 3.5.7
- Spring Data JPA / Hibernate
- PostgreSQL (via JDBC)
- **JWT (JSON Web Tokens)** para autenticação
- **BCrypt** para hash de senhas
- **RabbitMQ** para mensageria assíncrona
- Docker / Docker Compose
- Swagger / SpringDoc OpenAPI
- Lombok
- Spring Cache

---

## 🚀 Como Executar o Projeto

Clone o repositório:

```bash
git clone https://github.com/pedroonovais/timewise-api-java
cd timewise-api-java
```

### 1. Subir containers

```bash
docker compose up --build
```

Isso irá:

- Criar o container do **PostgreSQL**
- Criar o container do **RabbitMQ**
- Aplicar automaticamente o schema no banco (`ddl-auto: update`)
- Iniciar a API Spring Boot automaticamente (se Docker Compose estiver configurado)

### 2. Executar a API (Desenvolvimento Local)

Se preferir executar localmente (sem Docker para a API):

```bash
# Certifique-se de que os containers PostgreSQL e RabbitMQ estão rodando
docker compose up -d postgres rabbitmq

# Execute a aplicação Spring Boot
./mvnw spring-boot:run
```

Ou usando Maven:

```bash
mvn spring-boot:run
```

### 3. Acessar a API

**Swagger UI:**
- `http://localhost:8080/swagger-ui.html`

**API Base:**
- `http://localhost:8080/api`

**RabbitMQ Management UI:**
- `http://localhost:15672`
- Usuário: `timewise_user`
- Senha: `timewise_password`

---

## 🔄 Sistema de Mensageria Assíncrona

A API utiliza **RabbitMQ** para processar eventos de atividades de forma assíncrona, garantindo que o cálculo de scores diários seja feito sem bloquear as requisições HTTP.

### Como Funciona

1. **Evento Publicado**: Quando uma atividade é criada, atualizada ou deletada, um evento é publicado na fila `score.calcular`
2. **Processamento Assíncrono**: O `ScoreCalculatorListener` processa o evento de forma assíncrona
3. **Cálculo Automático**: O score diário é recalculado automaticamente para a data da atividade
4. **Cache Invalidado**: O cache de scores é invalidado para garantir dados atualizados

### Benefícios

- ✅ Requisições HTTP mais rápidas (não bloqueiam no cálculo)
- ✅ Processamento em background
- ✅ Retry automático em caso de falhas
- ✅ Escalabilidade horizontal

---

## 📊 Sistema de Score Diário

O sistema calcula automaticamente um **score de produtividade** (0-100) baseado nas atividades do usuário no dia.

### Como é Calculado

1. **Meta de Horas de Trabalho**: 6-8 horas é considerado ideal (score alto: 70-90 pontos)
   - Abaixo de 6h: score proporcional (0-70 pontos)
   - Acima de 8h: score reduzido (penalização por excesso)

2. **Bônus de Pausas**: Pausas adequadas (10-20% do tempo total) geram bônus de até 10 pontos
   - Ideal: 15% de pausa em relação ao tempo total

3. **Score Final**: Score base + bônus (limitado a 100 pontos)

### Exemplo

- **7 horas de trabalho** + **1 hora de pausa** (12.5% de pausa)
- Score base: ~90 pontos (dentro da meta ideal)
- Bônus: ~7.5 pontos (próximo do ideal de 15%)
- **Score final: ~97 pontos** ✅

---

## 🔐 Autenticação JWT

A API utiliza **JSON Web Tokens (JWT)** para autenticação e autorização. Todos os endpoints principais estão protegidos e requerem um token válido.

### 🚀 Como Começar

#### 1️⃣ Registrar um Novo Usuário

```bash
POST /api/auth/register
Content-Type: application/json

{
  "nome": "João Silva",
  "email": "joao@timewise.com",
  "senha": "senha123"
}
```

**Resposta (201 Created):**

```json
{
  "id": 1,
  "nome": "João Silva",
  "email": "joao@timewise.com"
}
```

#### 2️⃣ Fazer Login

```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "joao@timewise.com",
  "senha": "senha123"
}
```

**Resposta (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "usuario": {
    "id": 1,
    "nome": "João Silva",
    "email": "joao@timewise.com"
  }
}
```

#### 3️⃣ Usar o Token em Requisições

Após obter o token, inclua-o no header `Authorization` de todas as requisições:

```bash
GET /api/usuarios
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 🔑 Endpoints de Autenticação

| Método | Rota | Descrição | Autenticação |
|--------|------|-----------|--------------|
| POST | `/api/auth/register` | Registra um novo usuário | ❌ Não requer |
| POST | `/api/auth/login` | Autentica um usuário e retorna token JWT | ❌ Não requer |

### 🧪 Testando com Swagger

1. Acesse o Swagger em `http://localhost:8080/swagger-ui.html`
2. Registre-se ou faça login usando os endpoints de Auth
3. Copie o token retornado
4. Clique no botão **"Authorize"** 🔒 no canto superior direito
5. Digite: `Bearer {seu-token}` (substitua `{seu-token}` pelo token copiado)
6. Clique em **"Authorize"** e depois **"Close"**
7. Agora você pode testar todos os endpoints protegidos! ✅

### 📋 Exemplos Práticos

#### Exemplo com cURL:

```bash
# 1. Fazer login
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"joao@timewise.com","senha":"senha123"}' \
  | jq -r '.token')

# 2. Usar o token para acessar endpoint protegido
curl http://localhost:8080/api/usuarios \
  -H "Authorization: Bearer $TOKEN"
```

#### Exemplo com JavaScript/Fetch:

```javascript
// 1. Fazer login
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'joao@timewise.com',
    senha: 'senha123'
  })
});

const { token } = await response.json();

// 2. Usar o token
const usuarios = await fetch('http://localhost:8080/api/usuarios', {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

### 🔒 Segurança

- ✅ Senhas criptografadas com **BCrypt** (impossível reverter)
- ✅ Tokens JWT assinados digitalmente (HMAC-SHA256)
- ✅ Tokens válidos por **24 horas** (configurável)
- ✅ Validação automática em todas as requisições
- ✅ Todos os endpoints principais protegidos com `@PreAuthorize` ou filtros de segurança

---

## 📬 Endpoints da API

⚠️ **Atenção:** Todos os endpoints abaixo **requerem autenticação JWT** (exceto endpoints de Auth). Inclua o token no header: `Authorization: Bearer {token}`

### 🔐 Auth (`/api/auth`)

| Método | Rota | Descrição | Autenticação |
|--------|------|-----------|--------------|
| POST | `/api/auth/register` | Registra um novo usuário | ❌ Não requer |
| POST | `/api/auth/login` | Autentica um usuário e retorna token JWT | ❌ Não requer |

---

### 🔹 Usuario (`/api/usuarios`) 🔒

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/usuarios` | Retorna todos os usuários cadastrados com paginação |
| POST | `/api/usuarios` | Cadastra um novo usuário |
| DELETE | `/api/usuarios/{id}` | Remove um usuário pelo ID |
| GET | `/api/usuarios/{id}` | Retorna um usuário específico por ID |
| PUT | `/api/usuarios/{id}` | Atualiza os dados de um usuário existente |

**Parâmetros de Paginação:**
- `page` (padrão: 0) - Número da página
- `size` (padrão: 20, máximo: 100) - Tamanho da página
- `sort` (padrão: id,desc) - Ordenação

---

### 🔹 Tarefa (`/api/tarefas`) 🔒

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/tarefas` | Retorna todas as tarefas cadastradas com paginação |
| POST | `/api/tarefas` | Cadastra uma nova tarefa |
| DELETE | `/api/tarefas/{id}` | Remove uma tarefa pelo ID |
| GET | `/api/tarefas/{id}` | Retorna uma tarefa específica por ID |
| PUT | `/api/tarefas/{id}` | Atualiza os dados de uma tarefa existente |
| GET | `/api/tarefas/usuario/{usuarioId}` | Retorna todas as tarefas de um usuário específico |

**Parâmetros de Paginação:**
- `page` (padrão: 0) - Número da página
- `size` (padrão: 20, máximo: 100) - Tamanho da página
- `sort` (padrão: id,desc) - Ordenação

---

### 🔹 Atividade (`/api/atividades`) 🔒

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/atividades` | Retorna todas as atividades cadastradas com paginação |
| POST | `/api/atividades` | Cadastra uma nova atividade (dispara cálculo de score) |
| DELETE | `/api/atividades/{id}` | Remove uma atividade pelo ID (recalcula score) |
| GET | `/api/atividades/{id}` | Retorna uma atividade específica por ID |
| PUT | `/api/atividades/{id}` | Atualiza os dados de uma atividade existente (recalcula score) |
| GET | `/api/atividades/usuario/{usuarioId}` | Retorna todas as atividades de um usuário específico |

**Tipos de Atividade:**
- `TRABALHO` - Período de trabalho
- `PAUSA` - Período de pausa/descanso

**Parâmetros de Paginação:**
- `page` (padrão: 0) - Número da página
- `size` (padrão: 20, máximo: 100) - Tamanho da página
- `sort` (padrão: id,desc) - Ordenação

---

### 🔹 Score Diário (`/api/score-diario`) 🔒

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/score-diario/usuario/{usuarioId}` | Retorna todos os scores diários de um usuário com paginação |
| GET | `/api/score-diario/{id}` | Retorna um score diário específico por ID |
| GET | `/api/score-diario/usuario/{usuarioId}/data?dataTrabalho={data}` | Retorna o score diário de um usuário para uma data específica |

**Nota:** Scores são calculados automaticamente quando atividades são criadas, atualizadas ou deletadas. Não há endpoints para criar/editar scores manualmente.

**Parâmetros de Paginação:**
- `page` (padrão: 0) - Número da página
- `size` (padrão: 20, máximo: 100) - Tamanho da página
- `sort` (padrão: dataTrabalho,desc) - Ordenação

**Formato de Data:**
- `dataTrabalho` - Formato ISO: `YYYY-MM-DD` (ex: `2025-01-15`)

---

## 🔄 Fluxo de Exemplo Completo

Esta seção demonstra um fluxo completo de uso da API, desde a autenticação até o registro de atividades e consulta de scores.

### 🎯 Cenário: Registrar Atividades e Consultar Score

**Passo a passo:**

1. ✅ Autenticar no sistema
2. ✅ Criar uma tarefa
3. ✅ Registrar atividades de trabalho e pausa
4. ✅ Consultar o score diário calculado automaticamente

### 📋 Exemplo Completo com cURL

#### 1️⃣ Autenticar no Sistema

```bash
# Fazer login para obter o token JWT
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@timewise.com",
    "senha": "senha123"
  }' | jq -r '.token')

echo "Token obtido: $TOKEN"
```

#### 2️⃣ Criar uma Tarefa

```bash
# Criar uma nova tarefa
TAREFA_RESPONSE=$(curl -X POST http://localhost:8080/api/tarefas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Desenvolver feature de autenticação",
    "descricao": "Implementar JWT na API",
    "usuarioId": 1
  }')

# Extrair o ID da tarefa criada
TAREFA_ID=$(echo $TAREFA_RESPONSE | jq -r '.id')

echo "Tarefa criada com ID: $TAREFA_ID"
```

#### 3️⃣ Registrar Atividades de Trabalho e Pausa

```bash
# Registrar atividade de trabalho (9:00 - 12:00)
curl -X POST http://localhost:8080/api/atividades \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Desenvolvimento matinal",
    "usuarioId": 1,
    "tempoInicio": "2025-01-15T09:00:00",
    "tempoFim": "2025-01-15T12:00:00",
    "tipo": "TRABALHO"
  }'

# Registrar pausa para almoço (12:00 - 13:00)
curl -X POST http://localhost:8080/api/atividades \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Pausa para almoço",
    "usuarioId": 1,
    "tempoInicio": "2025-01-15T12:00:00",
    "tempoFim": "2025-01-15T13:00:00",
    "tipo": "PAUSA"
  }'

# Registrar atividade de trabalho (13:00 - 17:00)
curl -X POST http://localhost:8080/api/atividades \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Desenvolvimento vespertino",
    "usuarioId": 1,
    "tempoInicio": "2025-01-15T13:00:00",
    "tempoFim": "2025-01-15T17:00:00",
    "tipo": "TRABALHO"
  }'
```

**Nota:** Cada criação/atualização de atividade dispara automaticamente o cálculo do score diário via RabbitMQ.

#### 4️⃣ Consultar Score Diário

```bash
# Consultar score do dia 2025-01-15
curl -X GET "http://localhost:8080/api/score-diario/usuario/1/data?dataTrabalho=2025-01-15" \
  -H "Authorization: Bearer $TOKEN" | jq
```

**Resposta esperada (200 OK):**

```json
{
  "id": 1,
  "usuarioId": 1,
  "dataTrabalho": "2025-01-15",
  "valor": 97
}
```

**Cálculo do Score:**
- **7 horas de trabalho** (9h-12h + 13h-17h)
- **1 hora de pausa** (12h-13h)
- **Proporção de pausa:** 12.5% (próximo do ideal de 15%)
- **Score base:** ~90 pontos (dentro da meta de 6-8h)
- **Bônus:** ~7.5 pontos (pausa adequada)
- **Score final:** ~97 pontos ✅

### 🎨 Exemplo Completo com JavaScript/Fetch

```javascript
const API_BASE = 'http://localhost:8080';

// 1. Autenticar
const loginResponse = await fetch(`${API_BASE}/api/auth/login`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'joao@timewise.com',
    senha: 'senha123'
  })
});
const { token } = await loginResponse.json();

// 2. Criar tarefa
const tarefaResponse = await fetch(`${API_BASE}/api/tarefas`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    titulo: 'Desenvolver feature de autenticação',
    descricao: 'Implementar JWT na API',
    usuarioId: 1
  })
});
const tarefa = await tarefaResponse.json();

// 3. Registrar atividades
const atividades = [
  {
    nome: 'Desenvolvimento matinal',
    usuarioId: 1,
    tempoInicio: '2025-01-15T09:00:00',
    tempoFim: '2025-01-15T12:00:00',
    tipo: 'TRABALHO'
  },
  {
    nome: 'Pausa para almoço',
    usuarioId: 1,
    tempoInicio: '2025-01-15T12:00:00',
    tempoFim: '2025-01-15T13:00:00',
    tipo: 'PAUSA'
  },
  {
    nome: 'Desenvolvimento vespertino',
    usuarioId: 1,
    tempoInicio: '2025-01-15T13:00:00',
    tempoFim: '2025-01-15T17:00:00',
    tipo: 'TRABALHO'
  }
];

for (const atividade of atividades) {
  await fetch(`${API_BASE}/api/atividades`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(atividade)
  });
}

// 4. Consultar score (aguardar processamento assíncrono)
setTimeout(async () => {
  const scoreResponse = await fetch(
    `${API_BASE}/api/score-diario/usuario/1/data?dataTrabalho=2025-01-15`,
    {
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );
  const score = await scoreResponse.json();
  console.log('Score diário:', score);
}, 2000); // Aguarda 2 segundos para processamento assíncrono
```

### 📝 Notas Importantes

- **Autenticação obrigatória:** Todos os endpoints (exceto Auth) requerem o token JWT no header `Authorization: Bearer {token}`
- **Cálculo assíncrono:** O score é calculado de forma assíncrona via RabbitMQ. Pode levar alguns segundos para estar disponível
- **Formato de data/hora:** Use formato ISO 8601: `YYYY-MM-DDTHH:mm:ss` (ex: `2025-01-15T09:00:00`)
- **Tipos de atividade:** Apenas `TRABALHO` e `PAUSA` são válidos
- **Cache:** Scores são cacheados para melhor performance. O cache é invalidado automaticamente quando atividades são modificadas

---

## 🛠 Configuração do Banco de Dados

O schema do banco é criado/atualizado automaticamente pelo Hibernate (`ddl-auto: update`).

### Configuração Atual

- **Modo:** `update` - Cria/atualiza o schema conforme as entidades
- **Dialeto:** PostgreSQL
- **Show SQL:** Habilitado (para desenvolvimento)

### Outros Modos Disponíveis

No arquivo `application.yaml`, você pode alterar `ddl-auto`:

- `update`: Cria/atualiza o schema (recomendado para desenvolvimento)
- `create`: Recria o schema a cada inicialização (apaga dados existentes)
- `create-drop`: Cria ao iniciar e apaga ao encerrar (útil para testes)
- `validate`: Apenas valida o schema, não altera nada
- `none`: Desabilita a geração automática

---

## 💾 Cache

A API utiliza **Spring Cache** para otimizar consultas frequentes:

- **Scores por Usuário:** Cache de scores diários por usuário
- **Invalidação Automática:** Cache é invalidado quando atividades são modificadas

### Configuração

O cache está configurado em `CacheConfig.java` e pode ser ajustado conforme necessário.

---

## 📝 Notas

### 🆕 Funcionalidades Principais

- **Sistema de Score Diário** (Janeiro 2025)
  - ✅ Cálculo automático baseado em horas trabalhadas e pausas
  - ✅ Processamento assíncrono via RabbitMQ
  - ✅ Bônus por pausas adequadas
  - ✅ Meta ideal de 6-8 horas de trabalho

- **Autenticação JWT implementada** (Janeiro 2025)
  - ✅ Registro e login de usuários
  - ✅ Tokens JWT com expiração de 24 horas
  - ✅ Senhas criptografadas com BCrypt
  - ✅ Todos os endpoints protegidos

- **Sistema de Mensageria** (Janeiro 2025)
  - ✅ RabbitMQ para processamento assíncrono
  - ✅ Eventos de atividades publicados automaticamente
  - ✅ Cálculo de scores em background
  - ✅ Retry automático em caso de falhas

### 🔗 URLs Úteis

**Desenvolvimento Local:**
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Base**: `http://localhost:8080/api`
- **RabbitMQ Management**: `http://localhost:15672` (usuário: `timewise_user`, senha: `timewise_password`)

**Docker Compose:**
- **PostgreSQL**: `localhost:5432`
- **RabbitMQ AMQP**: `localhost:5672`
- **RabbitMQ Management**: `localhost:15672`

---

## 🧪 Executando os Testes

O projeto possui testes implementados com **JUnit** e **Spring Boot Test** para garantir a qualidade e confiabilidade do código.

### ⚡ Quick Start

Execute os testes:

```bash
mvn test
```

Ou usando o wrapper Maven:

```bash
./mvnw test
```

---

## 📚 Estrutura de Dados

### Entidades Principais

- **Usuario**: Usuários do sistema
- **Tarefa**: Tarefas criadas pelos usuários
- **Atividade**: Registros de trabalho e pausa
- **ScoreDiario**: Scores calculados automaticamente por dia

### Relacionamentos

- `Usuario` 1:N `Tarefa`
- `Usuario` 1:N `Atividade`
- `Usuario` 1:N `ScoreDiario`

---

## 🔧 Variáveis de Ambiente

As configurações podem ser sobrescritas via variáveis de ambiente:

- `SPRING_DATASOURCE_URL`: URL do banco de dados
- `SPRING_DATASOURCE_USERNAME`: Usuário do banco
- `SPRING_DATASOURCE_PASSWORD`: Senha do banco
- `SPRING_RABBITMQ_HOST`: Host do RabbitMQ
- `SPRING_RABBITMQ_PORT`: Porta do RabbitMQ
- `SPRING_RABBITMQ_USERNAME`: Usuário do RabbitMQ
- `SPRING_RABBITMQ_PASSWORD`: Senha do RabbitMQ
- `JWT_SECRET`: Chave secreta para assinatura JWT
- `JWT_EXPIRATION`: Tempo de expiração do token (em milissegundos)

