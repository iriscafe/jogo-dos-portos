package com.jogos.portos.service;

import com.jogos.portos.domain.Game;
import com.jogos.portos.domain.Player;
import com.jogos.portos.repository.GameRepository;
import com.jogos.portos.repository.PlayerRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository, SimpMessagingTemplate messagingTemplate) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private static final BigDecimal INCOME_PER_TURN = new BigDecimal("10.00");

    @Transactional
    public Game createGame(int maxPlayers) {
        Game game = new Game();
        game.setCode(generateCode());
        game.setMaxPlayers(maxPlayers);
        game.setCurrentTurn(1);
        game.setFinished(false);
        game.setCreatedAt(OffsetDateTime.now());
        return gameRepository.save(game);
    }

    @Transactional(readOnly = true)
    public Optional<Game> findByCode(String code) {
        return gameRepository.findByCode(code);
    }

    @Transactional
    public Player joinGame(String code, Player player) {
        Game game = gameRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));
        if (game.getPlayers().size() >= game.getMaxPlayers()) {
            throw new IllegalStateException("Partida cheia");
        }
        player.setGame(game);
        if (player.getMoney() == null) {
            player.setMoney(new BigDecimal("100.00"));
        }
        Player saved = playerRepository.save(player);
        messagingTemplate.convertAndSend("/topic/game/" + code, "player_joined");
        return saved;
    }

    @Transactional
    public Game nextTurn(String code) {
        Game game = gameRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));
        // renda automática por turno
        for (Player p : game.getPlayers()) {
            p.setMoney(p.getMoney().add(INCOME_PER_TURN));
        }
        game.setCurrentTurn(game.getCurrentTurn() + 1);
        Game saved = gameRepository.save(game);
        messagingTemplate.convertAndSend("/topic/game/" + code, "next_turn");
        return saved;
    }

    @Transactional
    public Game restart(String code) {
        Game game = gameRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));
        game.setCurrentTurn(1);
        game.setFinished(false);
        for (Player p : game.getPlayers()) {
            p.setMoney(new BigDecimal("100.00"));
        }
        Game saved = gameRepository.save(game);
        messagingTemplate.convertAndSend("/topic/game/" + code, "restarted");
        return saved;
    }

    @Transactional
    public Game finish(String code) {
        Game game = gameRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));
        game.setFinished(true);
        Game saved = gameRepository.save(game);
        messagingTemplate.convertAndSend("/topic/game/" + code, "finished");
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Game> listGames() {
        return gameRepository.findAll();
    }

    private String generateCode() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}


