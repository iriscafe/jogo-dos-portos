package com.jogos.portos.service;

import com.jogos.portos.domain.Game;
import com.jogos.portos.domain.Player;
import com.jogos.portos.domain.Color;
import com.jogos.portos.repository.GameRepository;
import com.jogos.portos.repository.PlayerRepository;
import com.jogos.portos.repository.ColorRepository;
import com.jogos.portos.web.dto.WebSocketMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ColorRepository colorRepository;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository, SimpMessagingTemplate messagingTemplate, ColorRepository colorRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.messagingTemplate = messagingTemplate;
        this.colorRepository = colorRepository;
    }

    private static final Double INCOME_PER_TURN = 10.0;
    private static final Double DEFAULT_START_MONEY = 100.0;
    private static final Integer DEFAULT_SHIPS = 45;

    @Transactional
    public Game createGame() {
        Game game = new Game();
        game.setStatus(com.jogos.portos.domain.GameStatus.CRIADO);
        return gameRepository.save(game);
    }

    @Transactional(readOnly = true)
    public Optional<Game> findById(Long id) {
        return gameRepository.findById(id);
    }

    @Transactional
    public Player joinGame(Long gameId, Player player) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));
        if (game.getPlayers().size() >= 5) { // máximo 5 jogadores conforme diagrama
            throw new IllegalStateException("Partida cheia");
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
            Set<Long> usedColorIds = game.getPlayers().stream()
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
        for (Player p : game.getPlayers()) {
            p.setDinheiro(100.0);
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
        Game saved = gameRepository.save(game);
        
        // Notificar via WebSocket que o jogo terminou
        messagingTemplate.convertAndSend("/topic/game/" + gameId, 
            WebSocketMessage.gameFinished(saved));
        
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Game> listGames() {
        return gameRepository.findAll();
    }
}