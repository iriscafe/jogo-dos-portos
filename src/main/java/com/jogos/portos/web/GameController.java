package com.jogos.portos.web;

import com.jogos.portos.domain.Game;
import com.jogos.portos.domain.Player;
import com.jogos.portos.service.GameService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<Game> create(@RequestParam @Min(2) @Max(5) int maxPlayers) {
        Game game = gameService.createGame(maxPlayers);
        return ResponseEntity.created(URI.create("/api/games/" + game.getCode())).body(game);
    }

    @GetMapping
    public List<Game> list() {
        return gameService.listGames();
    }

    @PostMapping("/{code}/join")
    public Player join(@PathVariable String code, @RequestBody Player player) {
        return gameService.joinGame(code, player);
    }

    @PostMapping("/{code}/next-turn")
    public Game nextTurn(@PathVariable String code) {
        return gameService.nextTurn(code);
    }

    @PostMapping("/{code}/restart")
    public Game restart(@PathVariable String code) {
        return gameService.restart(code);
    }

    @PostMapping("/{code}/finish")
    public Game finish(@PathVariable String code) {
        return gameService.finish(code);
    }
}


