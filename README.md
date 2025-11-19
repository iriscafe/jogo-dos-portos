# Jogo dos Portos 🚢

Sistema completo de jogo de tabuleiro competitivo que combina estratégia de aquisição de rotas navais com perguntas de múltipla escolha.

## 🎮 Visão Geral

O **Jogo dos Portos** é uma aplicação web completa que permite que 2-5 jogadores participem de partidas estratégicas onde:
- Os jogadores competem por rotas marítimas entre portos
- Respondem perguntas de conhecimento geral para ganhar dinheiro
- Gerenciam recursos (dinheiro, navios, rotas)
- Compram navios adicionais durante o jogo
- Alternam turnos de forma organizada
- O vencedor é determinado por pontos (soma dos pontos das rotas compradas)

## 🛠️ Tecnologias

### Backend
- **Spring Boot 3.5.6** - Framework principal
- **Java 17** - Linguagem de programação
- **Maven** - Gerenciamento de dependências
- **JPA/Hibernate** - Persistência de dados
- **MySQL 8.0** - Banco de dados principal
- **WebSockets (STOMP)** - Comunicação em tempo real
- **Swagger/OpenAPI** - Documentação da API

### Frontend
- **HTML5/CSS3/JavaScript** - Interface web responsiva
- **SockJS + STOMP** - Cliente WebSocket
- **Font Awesome** - Ícones
- **Design responsivo** - Funciona em desktop e mobile
- **Página de Regras** - Documentação completa das regras do jogo

### DevOps
- **Docker Compose** - Containerização completa
- **MySQL Container** - Banco de dados containerizado

## 🚀 Execução Rápida

### Opção 1: Docker Compose (Recomendado)
```bash
# Clone o repositório
git clone <repository-url>
cd jogo-dos-portos

# Subir banco MySQL e aplicação
docker compose up -d --build

# Acessar o jogo
open http://localhost:8080

# Acessar as regras
open http://localhost:8080/regras.html
```

## 📖 Regras do Jogo

O jogo possui uma página completa de regras acessível através do link "Regras do Jogo" na interface ou diretamente em `/regras.html`. As regras incluem:
- Objetivo do jogo e critérios de vitória
- Como jogar (3 ações por turno: comprar rota, comprar navios, responder pergunta)
- Sistema de pontos e desempate
- Dicas estratégicas
- Informações sobre navios e rotas

## 🔧 APIs Disponíveis

### REST Endpoints

#### Games (`/api/games`)

- `POST /api/games` - Criar nova partida
- `GET /api/games` - Listar todas as partidas
- `GET /api/games/{id}` - Obter partida por ID
- `POST /api/games/{id}/join` - Entrar em uma partida
- `POST /api/games/{id}/next-turn` - Avançar turno
- `POST /api/games/{id}/restart` - Reiniciar partida
- `POST /api/games/{id}/finish` - Finalizar partida
- `POST /api/games/players/{playerId}/buy-ships?quantidade={qtd}` - Comprar navios

#### Routes (`/api/routes`)

- `GET /api/routes` - Listar todas as rotas
- `POST /api/routes/buy?playerId={id}` - Comprar uma rota
- `POST /api/routes/{routeId}/sell?playerId={id}` - Vender uma rota

#### Players (`/api/players`)

- Controle de jogadores

#### Questions (`/api/questions`)

- Banco de perguntas de múltipla escolha

#### Colors (`/api/colors`)

- Gerenciamento de cores dos jogadores

### Documentação Interativa
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🔌 WebSocket - Comunicação em Tempo Real

A aplicação utiliza **WebSockets com STOMP** para comunicação em tempo real entre os jogadores, permitindo uma experiência de interação dinâmica e sincronizada.

### ⚙️ Configuração WebSocket

- **Endpoint STOMP**: `/ws`
- **Broker**: `/topic`
- **Prefixo de aplicação**: `/app`
- **Biblioteca Frontend**: `@stomp/stompjs` + `sockjs-client`

### 📡 Endpoints WebSocket Disponíveis

#### Mensagens de Entrada (Client → Server)
- `/app/game/join` - Entrar em um jogo
- `/app/game/next-turn` - Avançar turno
- `/app/game/answer-question` - Responder pergunta
- `/app/game/finish` - Finalizar jogo
- `/app/game/restart` - Reiniciar jogo
- `/app/game/get-random-question` - Obter pergunta aleatória
- `/app/game/buy-route` - Comprar uma rota
- `/app/game/buy-ships` - Comprar navios

#### Mensagens de Saída (Server → Client)
- `/topic/game/{gameId}` - Canal específico do jogo

### 📨 Tipos de Mensagens WebSocket

1. **GAME_UPDATE** - Estado do jogo atualizado
2. **PLAYER_JOINED** - Jogador entrou no jogo
3. **PLAYER_LEFT** - Jogador saiu do jogo
4. **TURN_CHANGED** - Turno mudou (inclui currentTurnIndex)
5. **ROUTE_PURCHASED** - Rota foi comprada
6. **QUESTION_ANSWERED** - Pergunta foi respondida
7. **GAME_FINISHED** - Jogo foi finalizado
8. **NEW_QUESTION** - Nova pergunta disponível
9. **ERROR** - Erro ocorreu

## 🧪 Testes

```bash
# Executar todos os testes
./mvnw test

# Executar com perfil de teste (H2)
./mvnw -Dspring-boot.run.profiles=test test

# Executar testes específicos
./mvnw test -Dtest=GameServiceTest
```

## 🏗️ Arquitetura do Sistema

### Backend (Spring Boot)

```
src/main/java/com/jogos/portos/
├── domain/           # Entidades JPA
│   ├── Game.java     # Partida com controle de turnos
│   ├── Player.java   # Jogador com recursos (dinheiro, navios, pontos)
│   ├── Question.java # Perguntas com alternativas
│   ├── Route.java    # Rotas marítimas entre portos
│   ├── Port.java     # Portos do mapa
│   ├── Color.java    # Cores dos jogadores
│   └── GameStatus.java # Status do jogo (CRIADO, EM_ANDAMENTO, FINALIZADO)
├── repository/        # Repositórios JPA
│   ├── GameRepository.java
│   ├── PlayerRepository.java
│   ├── RouteRepository.java
│   ├── PortRepository.java
│   └── ColorRepository.java
├── service/          # Lógica de negócio
│   ├── GameService.java      # Gerenciamento de partidas e compra de navios
│   ├── QuestionService.java  # Sistema de perguntas
│   ├── RouteService.java     # Sistema de rotas (compra/venda)
│   └── PortService.java      # Gerenciamento de portos
├── web/              # Controllers REST + WebSocket
│   ├── GameController.java      # API REST de partidas
│   ├── RouteController.java     # API REST de rotas
│   ├── WebSocketController.java # WebSocket handlers
│   └── dto/                     # DTOs para comunicação
│       └── WebSocketMessage.java
└── DataInitializer.java # População inicial do banco
```

### Frontend (JavaScript Modular)

```
src/main/resources/static/
├── index.html          # Página principal do jogo
├── regras.html         # Página de regras do jogo
├── css/
│   ├── styles.css      # Estilos principais
│   └── regras.css      # Estilos da página de regras
└── js/
    ├── app.js          # Inicialização da aplicação
    ├── state.js        # Gerenciamento de estado do jogo
    ├── websocket.js    # Conexão WebSocket
    ├── messageHandler.js # Processamento de mensagens WebSocket
    ├── game.js         # Ações do jogo (criar, entrar, turnos)
    ├── board.js        # Renderização do tabuleiro
    ├── routes.js       # Gerenciamento de rotas (compra, visualização)
    ├── ships.js        # Visualização de navios nas rotas
    ├── questions.js    # Sistema de perguntas
    ├── ui.js           # Atualização da interface do usuário
    └── notifications.js # Sistema de notificações
```

## 🎯 Funcionalidades Técnicas

### Sistema de Turnos

- **Controle Backend**: `Game.currentTurnIndex` gerencia turnos
- **Sincronização**: Frontend recebe `currentTurnIndex` via WebSocket
- **Indicadores Visuais**: "SEU TURNO" vs "Aguardando"
- **Ações por Turno**: Cada jogador pode realizar 1 das 3 ações:
  1. Comprar uma rota
  2. Comprar navios
  3. Responder uma pergunta

### Sistema de Portos e Rotas

- **Portos**: Entidades que representam cidades portuárias no mapa
- **Rotas**: Conexões entre portos com custo, pontos e cor
- **Relacionamentos**: Cada rota conecta um porto de origem a um porto de destino
- **Visualização**: Mapa interativo mostra portos e rotas com cores dos jogadores

### Sistema de Recursos

- **Dinheiro**: 
  - Ganha $20 por resposta correta
  - Perde $5 por resposta errada
  - Renda automática de $10 por turno para todos os jogadores
- **Navios**: 
  - Início: 6 navios por jogador
  - Necessários para comprar rotas (número de navios = pontos da rota)
  - Podem ser comprados durante o turno por $10 cada
  - Navios são consumidos ao comprar rotas
- **Rotas**: 
  - Sistema de aquisição de rotas marítimas entre portos
  - Cada rota tem custo em dinheiro e requer navios (igual aos pontos)
  - Rotas dão pontos ao jogador (baseado no valor da rota)
  - Cada rota só pode ser comprada por um jogador
- **Pontos**: 
  - Soma dos pontos de todas as rotas compradas
  - Determinam o vencedor ao final do jogo
  - Em caso de empate, o jogador com mais dinheiro vence

### Comunicação em Tempo Real

- **WebSocket STOMP**: Comunicação bidirecional
- **Fallback REST**: APIs REST como backup
- **Sincronização**: Estado consistente entre todos os jogadores
- **Atualizações em tempo real**: Compra de rotas, navios, respostas de perguntas e mudanças de turno são sincronizadas instantaneamente

### Sistema de Vitória

- **Critério Principal**: Jogador com mais pontos (soma dos pontos das rotas compradas)
- **Desempate**: Em caso de empate em pontos, o jogador com mais dinheiro vence
- **Empate Total**: Se pontos e dinheiro forem iguais, há empate
- **Finalização Automática**: O jogo finaliza automaticamente quando todas as rotas são compradas
- **Finalização Manual**: Qualquer jogador pode finalizar o jogo clicando em "Finalizar"

## 🚀 Deploy

### Docker Compose (Produção)

```bash
# Build e deploy
docker compose up -d --build

# Logs
docker compose logs -f jogo-dos-portos

# Parar
docker compose down
```

### Variáveis de Ambiente

```bash
# Banco de dados
MYSQL_HOST=mysql
MYSQL_DATABASE=jogo_portos
MYSQL_USER=root
MYSQL_PASSWORD=root

# Aplicação
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

---
