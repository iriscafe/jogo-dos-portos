package com.jogos.portos.service;

import com.jogos.portos.domain.Player;
import com.jogos.portos.domain.Route;
import com.jogos.portos.repository.PlayerRepository;
import com.jogos.portos.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final PlayerRepository playerRepository;

    public RouteService(RouteRepository routeRepository, PlayerRepository playerRepository) {
        this.routeRepository = routeRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public List<Route> findAll() {
        return routeRepository.findAll();
    }

    @Transactional
    public Route buyRoute(Long playerId, Route route) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado"));
        if (player.getDinheiro() < route.getCusto()) {
            throw new IllegalStateException("Saldo insuficiente");
        }
        route.setDono(player);
        player.setDinheiro(player.getDinheiro() - route.getCusto());
        playerRepository.save(player);
        return routeRepository.save(route);
    }

    @Transactional
    public void sellRoute(Long playerId, Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Rota não encontrada"));
        if (route.getDono() == null || !route.getDono().getId().equals(playerId)) {
            throw new IllegalStateException("Rota não pertence ao jogador");
        }
        Player player = route.getDono();
        Double refund = route.getCusto();
        route.setDono(null);
        routeRepository.save(route);
        player.setDinheiro(player.getDinheiro() + refund);
        playerRepository.save(player);
    }
}


