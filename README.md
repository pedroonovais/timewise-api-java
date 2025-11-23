# TimeWise API Java

**TimeWise API Java** é uma API desenvolvida em **Spring Boot** para o sistema de **Assistente Inteligente de Produtividade Saudável**, com foco no controle de atividades de trabalho e pausas, cálculo automático de scores diários de produtividade, gerenciamento de tarefas e insights via Inteligência Artificial.

A solução é modularizada em camadas e utiliza **JPA/Hibernate com PostgreSQL** rodando em **Docker Compose**. Conta também com **RabbitMQ** para processamento assíncrono de eventos, **Spring AI** para recursos generativos e documentação interativa via **Swagger**.

---

## 📌 Funcionalidades

- 🔐 **Autenticação JWT** com login seguro
- 🔒 **Segurança** com hash BCrypt e proteção de rotas
- 📊 **Cálculo automático de Score Diário** baseado em atividades de trabalho e pausas
- 🤖 **IA Generativa** para análise de produtividade e dicas personalizadas
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

## 👩‍💻 Participantes

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
- **config**: Configurações (Security, RabbitMQ, Cache, AI, etc.)
- **auth**: Componentes de autenticação (JWT, UserDetailsService)
- **messaging**: Sistema de mensageria (publishers, listeners, eventos)
- **enums**: Enumerações do sistema

---

## 💻 Tecnologias Utilizadas

- Java 17
- Spring Boot 3.3.5
- Spring Data JPA / Hibernate
- PostgreSQL (via JDBC)
- **JWT (JSON Web Tokens)** para autenticação
- **BCrypt** para hash de senhas
- **RabbitMQ** para mensageria assíncrona
- **Spring AI** para integração com LLMs (Groq/Llama 3)
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
- `http://localhost:8080`

**RabbitMQ Management UI:**
- `http://localhost:15672`
- Usuário: `timewise_user`
- Senha: `timewise_password`

---

## 🚀 Deploy em Nuvem

### Produção

**URL da API:** `https://seu-dominio.com`
**Swagger UI:** `https://seu-dominio.com/swagger-ui.html`

### Credenciais de Teste

- **Email:** `teste@example.com`
- **Senha:** `Teste123!@#`

### Variáveis de Ambiente Configuradas

- `SPRING_DATASOURCE_URL`: URL do PostgreSQL em produção
- `SPRING_DATASOURCE_USERNAME`: Usuário do banco
- `SPRING_DATASOURCE_PASSWORD`: Senha do banco
- `SPRING_RABBITMQ_HOST`: Host do RabbitMQ
- `SPRING_RABBITMQ_PORT`: Porta do RabbitMQ
- `SPRING_RABBITMQ_USERNAME`: Usuário do RabbitMQ
- `SPRING_RABBITMQ_PASSWORD`: Senha do RabbitMQ
- `JWT_SECRET`: Chave secreta JWT
- `GROQ_API_KEY`: Chave da API Groq para IA

---

## 🔄 Sistema de Mensageria Assíncrona

A API utiliza **RabbitMQ** para processar eventos de atividades de forma assíncrona, garantindo que o cálculo de scores diários seja feito sem bloquear as requisições HTTP.

### Como Funciona

1. **Evento Publicado**: Quando uma atividade é criada, atualizada ou deletada, um evento é publicado na fila `score.calcular`
2. **Processamento Assíncrono**: O `ScoreCalculatorListener` processa o evento de forma assíncrona
3. **Cálculo Automático**: O score diário é recalculado automaticamente para a data da atividade
4. **Cache Invalidado**: O cache de scores é invalidado para garantir dados atualizados

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

---

## 🔐 Autenticação JWT

A API utiliza **JSON Web Tokens (JWT)** para autenticação e autorização. Todos os endpoints principais estão protegidos e requerem um token válido.

### 🚀 Como Começar

#### 1️⃣ Registrar um Novo Usuário

```bash
POST /usuarios
Content-Type: application/json

{
  "nome": "João Silva",
  "email": "joao@timewise.com",
  "senha": "Senha123!@"
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
POST /auth/login
Content-Type: application/json

{
  "email": "joao@timewise.com",
  "senha": "Senha123!@"
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

Após obter o token, inclua-o no header `Authorization` de todas as requisições protegidas:

```bash
GET /usuarios
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 📬 Endpoints da API

⚠️ **Atenção:** Todos os endpoints abaixo (exceto criação de usuário e login) **requerem autenticação JWT**. Inclua o token no header: `Authorization: Bearer {token}`

### 🔐 Auth (`/auth`)

| Método | Rota | Descrição | Autenticação |
|--------|------|-----------|--------------|
| POST | `/auth/login` | Autentica um usuário e retorna token JWT | ❌ Não requer |

### 👤 Usuario (`/usuarios`)

| Método | Rota | Descrição | Autenticação |
|--------|------|-----------|--------------|
| GET | `/usuarios` | Retorna todos os usuários cadastrados com paginação | ✅ Requer |
| POST | `/usuarios` | Cadastra um novo usuário | ❌ Não requer |
| GET | `/usuarios/{id}` | Retorna um usuário específico por ID | ✅ Requer |
| PUT | `/usuarios/{id}` | Atualiza os dados de um usuário existente | ✅ Requer |
| DELETE | `/usuarios/{id}` | Remove um usuário pelo ID | ✅ Requer |

### 📝 Tarefa (`/tarefas`) 🔒

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/tarefas` | Retorna todas as tarefas cadastradas com paginação |
| POST | `/tarefas` | Cadastra uma nova tarefa |
| GET | `/tarefas/{id}` | Retorna uma tarefa específica por ID |
| PUT | `/tarefas/{id}` | Atualiza os dados de uma tarefa existente |
| DELETE | `/tarefas/{id}` | Remove uma tarefa pelo ID |
| GET | `/tarefas/usuario/{usuarioId}` | Retorna todas as tarefas de um usuário específico |

### ⏱️ Atividade (`/atividades`) 🔒

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/atividades` | Retorna todas as atividades cadastradas com paginação |
| POST | `/atividades` | Cadastra uma nova atividade (dispara cálculo de score) |
| GET | `/atividades/{id}` | Retorna uma atividade específica por ID |
| PUT | `/atividades/{id}` | Atualiza os dados de uma atividade existente (recalcula score) |
| DELETE | `/atividades/{id}` | Remove uma atividade pelo ID (recalcula score) |
| GET | `/atividades/usuario/{usuarioId}` | Retorna todas as atividades de um usuário específico |

**Tipos de Atividade:** `TRABALHO`, `PAUSA`

### 📈 Score Diário (`/score-diario`) 🔒

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/score-diario/usuario/{usuarioId}` | Retorna todos os scores diários de um usuário com paginação |
| GET | `/score-diario/{id}` | Retorna um score diário específico por ID |
| GET | `/score-diario/usuario/{usuarioId}/data` | Retorna o score de um usuário para uma data (`?dataTrabalho=YYYY-MM-DD`) |

### 🤖 AI (`/ai`) 🔒

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/ai/analise` | Gera uma análise de produtividade com IA baseada nos últimos 7 dias do usuário logado |

---

## 🔄 Fluxo de Exemplo Completo

### 1️⃣ Autenticar no Sistema

```bash
TOKEN=$(curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@timewise.com",
    "senha": "Senha123!@"
  }' | jq -r '.token')

echo "Token obtido: $TOKEN"
```

### 2️⃣ Criar uma Tarefa

```bash
TAREFA_RESPONSE=$(curl -X POST http://localhost:8080/tarefas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Desenvolver feature de autenticação",
    "descricao": "Implementar JWT na API",
    "usuarioId": 1
  }')
```

### 3️⃣ Registrar Atividades

```bash
curl -X POST http://localhost:8080/atividades \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Desenvolvimento matinal",
    "usuarioId": 1,
    "tempoInicio": "2025-01-15T09:00:00",
    "tempoFim": "2025-01-15T12:00:00",
    "tipo": "TRABALHO"
  }'
```

### 4️⃣ Consultar Score Diário

```bash
curl -X GET "http://localhost:8080/score-diario/usuario/1/data?dataTrabalho=2025-01-15" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### 5️⃣ Gerar Análise de Produtividade com IA

```bash
curl -X GET "http://localhost:8080/ai/analise" \
  -H "Authorization: Bearer $TOKEN" | jq
```

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
- `GROQ_API_KEY`: Chave da API Groq para funcionalidades de IA
