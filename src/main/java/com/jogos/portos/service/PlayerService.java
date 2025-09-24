package com.jogos.portos.service;

import com.jogos.portos.domain.Player;
import com.jogos.portos.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public List<Player> list() {
        return playerRepository.findAll();
    }

    @Transactional
    public Player save(Player p) {
        return playerRepository.save(p);
    }
}


