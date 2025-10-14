# Jogo dos Portos 🚢

Sistema completo de jogo de tabuleiro competitivo que combina estratégia de aquisição de rotas navais com perguntas de múltipla escolha.

## 🎮 Visão Geral

O **Jogo dos Portos** é uma aplicação web completa que permite que 2-5 jogadores participem de partidas estratégicas onde:
- Os jogadores competem por rotas marítimas
- Respondem perguntas de conhecimento geral para ganhar dinheiro
- Gerenciam recursos (dinheiro, navios, rotas)
- Alternam turnos de forma organizada

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
```
## 🔧 APIs Disponíveis

### REST Endpoints
- **Games**: `/api/games` - Gerenciamento de partidas
- **Players**: `/api/players` - Controle de jogadores  
- **Questions**: `/api/questions` - Banco de perguntas
- **Routes**: `/api/routes` - Sistema de rotas
- **Users**: `/api/users` - Gerenciamento de usuários

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
│   ├── Player.java   # Jogador com recursos
│   ├── Question.java # Perguntas com alternativas
│   └── Route.java    # Rotas marítimas
├── repository/        # Repositórios JPA
├── service/          # Lógica de negócio
│   ├── GameService.java      # Gerenciamento de partidas
│   ├── QuestionService.java  # Sistema de perguntas
│   └── RouteService.java     # Sistema de rotas
├── web/              # Controllers REST + WebSocket
│   ├── GameController.java      # API REST
│   ├── WebSocketController.java # WebSocket handlers
│   └── dto/                     # DTOs para comunicação
└── DataInitializer.java # População inicial do banco
```

## 🎯 Funcionalidades Técnicas

### Sistema de Turnos
- **Controle Backend**: `Game.currentTurnIndex` gerencia turnos
- **Sincronização**: Frontend recebe `currentTurnIndex` via WebSocket
- **Indicadores Visuais**: "SEU TURNO" vs "Aguardando"

### Sistema de Recursos
- **Dinheiro**: Ganha $20 por resposta correta, perde $5 por erro
- **Renda Automática**: $10 por turno para todos os jogadores
- **Navios**: Controle de frota disponível
- **Rotas**: Sistema de aquisição de rotas marítimas

### Comunicação em Tempo Real
- **WebSocket STOMP**: Comunicação bidirecional
- **Fallback REST**: APIs REST como backup
- **Sincronização**: Estado consistente entre todos os jogadores

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
