package com.jogos.portos.service;

import com.jogos.portos.domain.Game;
import com.jogos.portos.domain.Player;
import com.jogos.portos.repository.GameRepository;
import com.jogos.portos.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    private static final Double INCOME_PER_TURN = 10.0;

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
            player.setDinheiro(100.0);
        }
        Player saved = playerRepository.save(player);
        return saved;
    }

    @Transactional
    public Game nextTurn(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));
        // renda automática por turno
        for (Player p : game.getPlayers()) {
            p.setDinheiro(p.getDinheiro() + INCOME_PER_TURN);
        }
        Game saved = gameRepository.save(game);
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
        return saved;
    }

    @Transactional
    public Game finish(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));
        game.setStatus(com.jogos.portos.domain.GameStatus.FINALIZADO);
        Game saved = gameRepository.save(game);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Game> listGames() {
        return gameRepository.findAll();
    }
}