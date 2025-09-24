package com.jogos.portos.service;

import com.jogos.portos.domain.Player;
import com.jogos.portos.domain.Route;
import com.jogos.portos.repository.PlayerRepository;
import com.jogos.portos.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final PlayerRepository playerRepository;

    public RouteService(RouteRepository routeRepository, PlayerRepository playerRepository) {
        this.routeRepository = routeRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public Route buyRoute(Long playerId, Route route) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado"));
        if (player.getMoney().compareTo(route.getPrice()) < 0) {
            throw new IllegalStateException("Saldo insuficiente");
        }
        route.setOwner(player);
        player.setMoney(player.getMoney().subtract(route.getPrice()));
        playerRepository.save(player);
        return routeRepository.save(route);
    }

    @Transactional
    public void sellRoute(Long playerId, Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Rota não encontrada"));
        if (route.getOwner() == null || !route.getOwner().getId().equals(playerId)) {
            throw new IllegalStateException("Rota não pertence ao jogador");
        }
        Player player = route.getOwner();
        BigDecimal refund = route.getPrice();
        route.setOwner(null);
        routeRepository.save(route);
        player.setMoney(player.getMoney().add(refund));
        playerRepository.save(player);
    }
}


