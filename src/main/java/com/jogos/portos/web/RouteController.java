package com.jogos.portos.web;

import com.jogos.portos.domain.Route;
import com.jogos.portos.service.RouteService;
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
    public Route buy(@RequestParam Long playerId, @RequestBody Route route) {
        return routeService.buyRoute(playerId, route);
    }

    @PostMapping("/{routeId}/sell")
    public Map<String, String> sell(@PathVariable Long routeId, @RequestParam Long playerId) {
        routeService.sellRoute(playerId, routeId);
        return Map.of("status", "ok");
    }
}


