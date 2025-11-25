package com.jogos.portos.web;

import com.jogos.portos.domain.Game;
import com.jogos.portos.domain.Player;
import com.jogos.portos.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<Game> create() {
        Game game = gameService.createGame();
        return ResponseEntity.created(URI.create("/api/games/" + game.getId())).body(game);
    }

    @GetMapping
    public List<Game> list() {
        return gameService.listGames();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Game> getById(@PathVariable Long id) {
        return gameService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> join(@PathVariable Long id, @RequestBody Player player) {
        try {
            Player joinedPlayer = gameService.joinGame(id, player);
            return ResponseEntity.ok(joinedPlayer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao entrar na partida: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/next-turn")
    public Game nextTurn(@PathVariable Long id) {
        return gameService.nextTurn(id);
    }

    @PostMapping("/{id}/restart")
    public Game restart(@PathVariable Long id) {
        return gameService.restart(id);
    }

    @PostMapping("/{id}/finish")
    public Game finish(@PathVariable Long id) {
        return gameService.finish(id);
    }
}