package com.jogos.portos.service;

import com.jogos.portos.domain.Game;
import com.jogos.portos.domain.Player;
import com.jogos.portos.domain.Color;
import com.jogos.portos.domain.Route;
import com.jogos.portos.repository.GameRepository;
import com.jogos.portos.repository.PlayerRepository;
import com.jogos.portos.repository.ColorRepository;
import com.jogos.portos.repository.RouteRepository;
import com.jogos.portos.web.dto.WebSocketMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ColorRepository colorRepository;
    private final RouteRepository routeRepository;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository, SimpMessagingTemplate messagingTemplate, ColorRepository colorRepository, RouteRepository routeRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.messagingTemplate = messagingTemplate;
        this.colorRepository = colorRepository;
        this.routeRepository = routeRepository;
    }

    private static final Double INCOME_PER_TURN = 10.0;
    private static final Double DEFAULT_START_MONEY = 50.0;
    private static final Integer DEFAULT_SHIPS = 45;

    @Transactional
    public Game createGame() {
        List<Route> baseRoutes = routeRepository.findAll().stream()
                .filter(route -> route.getGame() == null && route.getDono() != null)
                .collect(Collectors.toList());
        
        for (Route route : baseRoutes) {
            route.setDono(null);
            routeRepository.save(route);
        }
        
        Game game = new Game();
        game.setStatus(com.jogos.portos.domain.GameStatus.CRIADO);
        return gameRepository.save(game);
    }

    @Transactional(readOnly = true)
    public Optional<Game> findById(Long id) {
        Optional<Game> gameOpt = gameRepository.findById(id);
        if (gameOpt.isPresent()) {
            Game game = gameOpt.get();
            game.getRotas().size(); // Isso força o carregamento lazy
            
            if (game.getRotas().isEmpty()) {
                List<Route> baseRoutes = routeRepository.findAll().stream()
                        .filter(route -> route.getGame() == null)
                        .collect(Collectors.toList());
                
                for (Route route : baseRoutes) {
                    if (route.getPortoOrigem() != null) {
                        route.getPortoOrigem().getId(); // Força carregamento
                        route.getPortoOrigem().getCidade(); // Força carregamento
                    }
                    if (route.getPortoDestino() != null) {
                        route.getPortoDestino().getId(); // Força carregamento
                        route.getPortoDestino().getCidade(); // Força carregamento
                    }
                    if (route.getCor() != null) {
                        route.getCor().getId(); // Força carregamento
                        route.getCor().getNome(); // Força carregamento
                    }
                }
                
                for (Route route : baseRoutes) {
                    if (route.getDono() != null) {
                        route.getDono().getId();
                        route.getDono().getName();
                        if (route.getDono().getCor() != null) {
                            route.getDono().getCor().getId();
                            route.getDono().getCor().getNome();
                        }
                    }
                }
                
                game.getRotas().addAll(baseRoutes);
            } else {
                for (Route route : game.getRotas()) {
                    if (route.getPortoOrigem() != null) {
                        route.getPortoOrigem().getId();
                        route.getPortoOrigem().getCidade();
                    }
                    if (route.getPortoDestino() != null) {
                        route.getPortoDestino().getId();
                        route.getPortoDestino().getCidade();
                    }
                    if (route.getCor() != null) {
                        route.getCor().getId();
                        route.getCor().getNome();
                    }
                    if (route.getDono() != null) {
                        route.getDono().getId();
                        route.getDono().getName();
                        // Forçar carregamento da cor do dono
                        if (route.getDono().getCor() != null) {
                            route.getDono().getCor().getId();
                            route.getDono().getCor().getNome();
                        }
                    }
                }
            }
        }
        return gameOpt;
    }

    @Transactional
    public Player joinGame(Long gameId, Player player) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));
        
        // Buscar jogadores diretamente do repositório para garantir que estão carregados
        List<Player> players = playerRepository.findAll().stream()
                .filter(p -> p.getGame() != null && p.getGame().getId().equals(gameId))
                .collect(Collectors.toList());
        
        if (players.size() >= 5) { // máximo 5 jogadores conforme diagrama
            throw new IllegalStateException("Partida cheia");
        }
        
        // Verificar se já existe um jogador com o mesmo nome no jogo
        // Se existir, permitir que entre novamente (reconexão)
        String playerName = player.getName();
        if (playerName != null && !playerName.trim().isEmpty()) {
            Player existingPlayer = players.stream()
                    .filter(p -> p.getName() != null && 
                            p.getName().trim().equalsIgnoreCase(playerName.trim()))
                    .findFirst()
                    .orElse(null);
            
            if (existingPlayer != null) {
                // Jogador já existe - retornar o jogador existente em vez de criar novo
                // Isso permite reconexão
                Game gameWithPlayers = gameRepository.findById(gameId).orElse(game);
                // Notificar via WebSocket que o jogador reconectou
                messagingTemplate.convertAndSend("/topic/game/" + gameId, 
                    WebSocketMessage.playerJoined(existingPlayer, gameWithPlayers));
                return existingPlayer;
            }
        }
        
        player.setGame(game);
        if (player.getDinheiro() == null) {
            player.setDinheiro(DEFAULT_START_MONEY);
        }
        if (player.getNaviosDisponiveis() == null) {
            player.setNaviosDisponiveis(DEFAULT_SHIPS);
        }
        if (player.getCor() == null) {
            // atribuir automaticamente uma cor disponível que não esteja em uso na partida
            Set<Long> usedColorIds = players.stream()
                    .map(Player::getCor)
                    .filter(c -> c != null && c.getId() != null)
                    .map(Color::getId)
                    .collect(Collectors.toSet());
            Color available = colorRepository.findAll().stream()
                    .filter(c -> c.getId() != null && !usedColorIds.contains(c.getId()))
                    .findFirst()
                    .orElse(null);
            player.setCor(available);
        }
        Player saved = playerRepository.save(player);
        
        // Notificar via WebSocket que um jogador entrou
        messagingTemplate.convertAndSend("/topic/game/" + gameId, 
            WebSocketMessage.playerJoined(saved, game));
        
        return saved;
    }

    @Transactional
    public Game nextTurn(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));
        
        // Se o jogo ainda está CRIADO, mudar para EM_ANDAMENTO quando começar o primeiro turno
        if (game.getStatus() == com.jogos.portos.domain.GameStatus.CRIADO) {
            game.iniciarPartida();
        }
        
        // Avançar turno
        game.nextTurn();
        
        // renda automática por turno
        for (Player p : game.getPlayers()) {
            p.setDinheiro(p.getDinheiro() + INCOME_PER_TURN);
        }
        Game saved = gameRepository.save(game);
        
        // Notificar via WebSocket que o turno mudou
        messagingTemplate.convertAndSend("/topic/game/" + gameId, 
            WebSocketMessage.turnChanged(saved));
        
        return saved;
    }

    @Transactional
    public Game restart(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));
        game.setStatus(com.jogos.portos.domain.GameStatus.CRIADO);
        game.setCurrentTurnIndex(0);
        
        // Resetar recursos dos jogadores
        for (Player p : game.getPlayers()) {
            p.setDinheiro(DEFAULT_START_MONEY);
            p.setNaviosDisponiveis(DEFAULT_SHIPS);
            playerRepository.save(p);
        }
        
        // Resetar rotas base (sem game_id) que possam ter sido compradas
        // Nota: como as rotas base são compartilhadas, isso pode afetar outros jogos
        // Uma solução futura seria criar cópias das rotas para cada jogo
        List<Route> baseRoutes = routeRepository.findAll().stream()
                .filter(route -> route.getGame() == null && route.getDono() != null)
                .collect(Collectors.toList());
        
        for (Route route : baseRoutes) {
            route.setDono(null);
            routeRepository.save(route);
        }
        
        Game saved = gameRepository.save(game);
        
        // Notificar via WebSocket que o jogo foi reiniciado
        messagingTemplate.convertAndSend("/topic/game/" + gameId, 
            WebSocketMessage.gameUpdate(saved));
        
        return saved;
    }

    @Transactional
    public Game finish(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));
        game.setStatus(com.jogos.portos.domain.GameStatus.FINALIZADO);
        
        // Calcular pontuação final baseada nas rotas possuídas
        // O ganhador é o jogador com mais pontos (soma dos pontos das rotas que possui)
        // Buscar rotas diretamente do repositório para garantir que todas sejam consideradas
        List<Route> todasRotas = routeRepository.findAll().stream()
                .filter(route -> route.getGame() != null && route.getGame().getId().equals(gameId))
                .collect(Collectors.toList());
        
        // Se não houver rotas associadas ao jogo, usar rotas base (sem game_id)
        if (todasRotas.isEmpty()) {
            todasRotas = routeRepository.findAll().stream()
                    .filter(route -> route.getGame() == null)
                    .collect(Collectors.toList());
        }
        
        // Calcular pontuações
        Map<Player, Integer> pontuacoes = new HashMap<>();
        for (Player player : game.getPlayers()) {
            int pontos = 0;
            for (Route rota : todasRotas) {
                if (rota.getDono() != null && rota.getDono().getId().equals(player.getId())) {
                    pontos += rota.getPontos();
                }
            }
            pontuacoes.put(player, pontos);
        }
        
        // Encontrar o jogador com maior pontuação
        // Se houver empate em pontos, usar dinheiro como critério de desempate
        // Se pontos e dinheiro forem iguais, declarar empate
        List<Player> candidatosVencedores = new ArrayList<>();
        int maiorPontuacao = -1;
        double maiorDinheiro = -1;
        
        // Primeiro, encontrar a maior pontuação
        for (Map.Entry<Player, Integer> entry : pontuacoes.entrySet()) {
            if (entry.getValue() > maiorPontuacao) {
                maiorPontuacao = entry.getValue();
            }
        }
        
        // Encontrar todos os jogadores com a maior pontuação
        for (Map.Entry<Player, Integer> entry : pontuacoes.entrySet()) {
            if (entry.getValue() == maiorPontuacao) {
                candidatosVencedores.add(entry.getKey());
            }
        }
        
        Player ganhador = null;
        boolean empate = false;
        
        if (candidatosVencedores.size() == 1) {
            // Apenas um jogador com maior pontuação
            ganhador = candidatosVencedores.get(0);
        } else if (candidatosVencedores.size() > 1) {
            // Empate em pontos - usar dinheiro para desempatar
            // Primeiro, encontrar o maior dinheiro entre os candidatos
            for (Player candidato : candidatosVencedores) {
                if (candidato.getDinheiro() > maiorDinheiro) {
                    maiorDinheiro = candidato.getDinheiro();
                }
            }
            
            // Encontrar jogadores com maior dinheiro
            List<Player> candidatosPorDinheiro = new ArrayList<>();
            for (Player candidato : candidatosVencedores) {
                if (candidato.getDinheiro() == maiorDinheiro) {
                    candidatosPorDinheiro.add(candidato);
                }
            }
            
            if (candidatosPorDinheiro.size() == 1) {
                // Desempate por dinheiro funcionou - temos um vencedor
                ganhador = candidatosPorDinheiro.get(0);
            } else {
                // Empate total (pontos e dinheiro iguais)
                // Usar os candidatos por dinheiro como vencedores do empate
                empate = true;
                candidatosVencedores = candidatosPorDinheiro; // Atualizar para os que empataram em dinheiro também
            }
        }
        
        Game saved = gameRepository.save(game);
        
        // Criar dados do resultado final incluindo pontuações e ganhador
        Map<String, Object> resultadoFinal = new HashMap<>();
        resultadoFinal.put("game", saved);
        resultadoFinal.put("pontuacoes", pontuacoes.entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey().getId(),
                Map.Entry::getValue
            )));
        resultadoFinal.put("empate", empate);
        
        if (empate) {
            // Em caso de empate, listar todos os vencedores
            List<Map<String, Object>> vencedores = new ArrayList<>();
            for (Player vencedor : candidatosVencedores) {
                Map<String, Object> vencedorInfo = new HashMap<>();
                vencedorInfo.put("id", vencedor.getId());
                vencedorInfo.put("nome", vencedor.getName());
                vencedorInfo.put("pontuacao", maiorPontuacao);
                vencedorInfo.put("dinheiro", vencedor.getDinheiro());
                vencedores.add(vencedorInfo);
            }
            resultadoFinal.put("vencedores", vencedores);
            String nomesVencedores = candidatosVencedores.stream()
                    .map(Player::getName)
                    .collect(Collectors.joining(", "));
            resultadoFinal.put("mensagemEmpate", "Empate! " + nomesVencedores + " empataram com " + maiorPontuacao + " pontos e $" + String.format("%.0f", maiorDinheiro));
        } else if (ganhador != null) {
            resultadoFinal.put("ganhadorId", ganhador.getId());
            resultadoFinal.put("ganhadorNome", ganhador.getName());
            resultadoFinal.put("pontuacaoGanhador", maiorPontuacao);
            resultadoFinal.put("dinheiroGanhador", ganhador.getDinheiro());
        }
        
        // Notificar via WebSocket que o jogo terminou com o resultado
        messagingTemplate.convertAndSend("/topic/game/" + gameId, 
            WebSocketMessage.gameFinishedWithResult(resultadoFinal, gameId));
        
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Game> listGames() {
        return gameRepository.findAll();
    }
}