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

## WebSocket

- **Endpoint STOMP**: `/ws`
- **Broker**: `/topic`

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

