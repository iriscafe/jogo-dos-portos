package com.jogos.portos.web;

import com.jogos.portos.domain.Route;
import com.jogos.portos.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public List<Route> list() {
        return routeService.findAll();
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buy(@RequestParam Long playerId, @RequestBody Route route) {
        try {
            Route boughtRoute = routeService.buyRoute(playerId, route);
            return ResponseEntity.ok(boughtRoute);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao comprar rota: " + e.getMessage()));
        }
    }

    @PostMapping("/{routeId}/sell")
    public Map<String, String> sell(@PathVariable Long routeId, @RequestParam Long playerId) {
        routeService.sellRoute(playerId, routeId);
        return Map.of("status", "ok");
    }
}


