package com.jogos.portos.service;

import com.jogos.portos.domain.Game;
import com.jogos.portos.domain.Player;
import com.jogos.portos.domain.Route;
import com.jogos.portos.repository.GameRepository;
import com.jogos.portos.repository.PlayerRepository;
import com.jogos.portos.repository.RouteRepository;
import com.jogos.portos.web.dto.WebSocketMessage;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameRepository gameRepository;
    private final ApplicationContext applicationContext;

    public RouteService(RouteRepository routeRepository, PlayerRepository playerRepository, SimpMessagingTemplate messagingTemplate, GameRepository gameRepository, ApplicationContext applicationContext) {
        this.routeRepository = routeRepository;
        this.playerRepository = playerRepository;
        this.messagingTemplate = messagingTemplate;
        this.gameRepository = gameRepository;
        this.applicationContext = applicationContext;
    }

    @Transactional(readOnly = true)
    public List<Route> findAll() {
        return routeRepository.findAll();
    }

    @Transactional
    public Route buyRoute(Long playerId, Route route) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado"));
        
        // Buscar a rota do banco usando o ID fornecido
        Route routeToBuy = routeRepository.findById(route.getId())
                .orElseThrow(() -> new IllegalArgumentException("Rota não encontrada"));
        
        // Verificar se a rota já tem dono
        if (routeToBuy.getDono() != null) {
            throw new IllegalStateException("Esta rota já foi comprada por outro jogador");
        }
        
        if (player.getDinheiro() < routeToBuy.getCusto()) {
            throw new IllegalStateException("Saldo insuficiente");
        }
        
        // Verificar se o jogador tem navios suficientes
        // O número de navios necessários é igual aos pontos da rota (como no Ticket to Ride)
        int naviosNecessarios = routeToBuy.getPontos();
        if (player.getNaviosDisponiveis() < naviosNecessarios) {
            throw new IllegalStateException("Navios insuficientes. Você precisa de " + naviosNecessarios + " navios, mas tem apenas " + player.getNaviosDisponiveis());
        }
        
        routeToBuy.setDono(player);
        player.setDinheiro(player.getDinheiro() - routeToBuy.getCusto());
        // Deduzir navios ao comprar a rota (como trens no Ticket to Ride)
        player.setNaviosDisponiveis(player.getNaviosDisponiveis() - naviosNecessarios);
        playerRepository.save(player);
        Route savedRoute = routeRepository.save(routeToBuy);
        
        // Notificar via WebSocket que uma rota foi comprada e atualizar jogo completo
        if (player.getGame() != null) {
            Long gameId = player.getGame().getId();
            messagingTemplate.convertAndSend("/topic/game/" + gameId, 
                WebSocketMessage.routePurchased(savedRoute, gameId, playerId));
            
            // Atualizar jogo completo para sincronizar dinheiro e navios em tempo real
            // Usar findById do GameService para garantir que rotas sejam carregadas corretamente
            GameService gameService = applicationContext.getBean(GameService.class);
            gameService.findById(gameId).ifPresent(game -> {
                messagingTemplate.convertAndSend("/topic/game/" + gameId, 
                    WebSocketMessage.gameUpdate(game));
            });
            
            // Verificar se todas as rotas foram compradas e finalizar jogo automaticamente
            List<Route> baseRoutes = routeRepository.findAll().stream()
                    .filter(r -> r.getGame() == null)
                    .collect(Collectors.toList());
            
            boolean todasCompradas = baseRoutes.stream()
                    .allMatch(r -> r.getDono() != null);
            
            if (todasCompradas) {
                // Buscar o jogo novamente para verificar status
                Game gameParaFinalizar = gameRepository.findById(gameId).orElse(null);
                if (gameParaFinalizar != null && gameParaFinalizar.getStatus() != com.jogos.portos.domain.GameStatus.FINALIZADO) {
                    // Finalizar jogo automaticamente (usando ApplicationContext para evitar dependência circular)
                    GameService gameServiceBean = applicationContext.getBean(GameService.class);
                    gameServiceBean.finish(gameId);
                }
            }
        }
        
        return savedRoute;
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
        // Devolver navios ao vender rota
        int naviosDevolvidos = route.getPontos();
        route.setDono(null);
        routeRepository.save(route);
        player.setDinheiro(player.getDinheiro() + refund);
        player.setNaviosDisponiveis(player.getNaviosDisponiveis() + naviosDevolvidos);
        playerRepository.save(player);
    }
}


