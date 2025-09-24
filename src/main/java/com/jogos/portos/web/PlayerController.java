package com.jogos.portos.web;

import com.jogos.portos.domain.Player;
import com.jogos.portos.service.PlayerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public List<Player> list() {
        return playerService.list();
    }

    @PostMapping
    public Player create(@RequestBody Player p) {
        return playerService.save(p);
    }
}


