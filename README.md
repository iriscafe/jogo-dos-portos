# Jogo dos Portos - Backend

Sistema de jogo de tabuleiro competitivo que combina estratégia de aquisição de rotas navais com perguntas de múltipla escolha.

## Tecnologias

- **Spring Boot** - Framework principal
- **Java 17** - Linguagem de programação
- **Maven** - Gerenciamento de dependências
- **JPA/Hibernate** - Persistência de dados
- **MySQL** - Banco de dados principal
- **H2** - Banco de dados para testes
- **WebSockets** - Comunicação em tempo real
- **Swagger/OpenAPI** - Documentação da API
- **Docker** - Containerização

## Funcionalidades Implementadas

### Backend Completo
- **Modo Multijogador**: Suporte para 2-5 jogadores
- **Banco de Perguntas**: Sistema de perguntas de múltipla escolha
- **Gerenciamento de Partidas**: Criação, configuração, reinício e encerramento
- **Sistema de Dinheiro e Rotas**: Controle financeiro e aquisição de rotas
- **Pontuação Final**: Cálculo baseado em rotas conquistadas

### APIs Disponíveis
- **Games**: `/api/games` - Gerenciamento de partidas
- **Players**: `/api/players` - Controle de jogadores
- **Questions**: `/api/questions` - Banco de perguntas
- **Routes**: `/api/routes` - Sistema de rotas
- **Users**: `/api/users` - Gerenciamento de usuários

## Execução

### Opção 1: Docker Compose (Recomendado)
```bash
# Subir banco MySQL e aplicação
docker compose up -d --build

# Aplicação disponível em: http://localhost:8080
# MySQL disponível em: localhost:3306
```

## Documentação da API

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## WebSocket - Comunicação em Tempo Real

A aplicação utiliza WebSockets para comunicação em tempo real entre os jogadores, permitindo uma experiência de interação dinâmica.

### Configuração WebSocket

- **Endpoint STOMP**: `/ws`
- **Broker**: `/topic`
- **Prefixo de aplicação**: `/app`

### Endpoints WebSocket Disponíveis

#### Mensagens de Entrada (Client → Server)
- `/app/game/join` - Entrar em um jogo
- `/app/game/next-turn` - Avançar turno
- `/app/game/answer-question` - Responder pergunta
- `/app/game/finish` - Finalizar jogo
- `/app/game/restart` - Reiniciar jogo
- `/app/game/get-random-question` - Obter pergunta aleatória

#### Mensagens de Saída (Server → Client)
- `/topic/game/{gameId}` - Canal específico do jogo

### Tipos de Mensagens WebSocket

1. **GAME_UPDATE** - Estado do jogo atualizado
2. **PLAYER_JOINED** - Jogador entrou no jogo
3. **PLAYER_LEFT** - Jogador saiu do jogo
4. **TURN_CHANGED** - Turno mudou
5. **ROUTE_PURCHASED** - Rota foi comprada
6. **QUESTION_ANSWERED** - Pergunta foi respondida
7. **GAME_FINISHED** - Jogo foi finalizado
8. **NEW_QUESTION** - Nova pergunta disponível
9. **ERROR** - Erro ocorreu

### Exemplo de Uso

```javascript
// Conectar ao WebSocket
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // Inscrever-se no canal do jogo
    stompClient.subscribe('/topic/game/1', function(message) {
        const data = JSON.parse(message.body);
        console.log('Mensagem recebida:', data);
    });
    
    // Entrar em um jogo
    stompClient.send('/app/game/join', {}, JSON.stringify({
        gameId: 1,
        playerName: 'João'
    }));
});
```

## Configuração do Banco

### Docker (Padrão)
- Host: `mysql` (container)
- Database: `jogo_portos`
- User: `root`
- Password: `root`

### Local
- Host: `localhost:3306`
- Database: `jogo_portos`
- User: `root`
- Password: `root`

## Testes

```bash
# Executar todos os testes
./mvnw test

# Executar com perfil de teste (H2)
./mvnw -Dspring-boot.run.profiles=test test
```

