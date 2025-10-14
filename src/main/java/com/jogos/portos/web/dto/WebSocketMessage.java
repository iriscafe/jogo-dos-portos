package com.jogos.portos.web.dto;

import com.jogos.portos.domain.Game;
import com.jogos.portos.domain.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    private String type;
    private Object data;
    private String message;
    private Long gameId;
    private Long playerId;

    public static WebSocketMessage gameUpdate(Game game) {
        return new WebSocketMessage("GAME_UPDATE", game, "Game state updated", game.getId(), null);
    }

    public static WebSocketMessage playerJoined(Player player, Game game) {
        return new WebSocketMessage("PLAYER_JOINED", player, "Player joined the game", game.getId(), player.getId());
    }

    public static WebSocketMessage playerLeft(Long playerId, Long gameId) {
        return new WebSocketMessage("PLAYER_LEFT", null, "Player left the game", gameId, playerId);
    }

    public static WebSocketMessage turnChanged(Game game) {
        return new WebSocketMessage("TURN_CHANGED", game, "Turn changed", game.getId(), null);
    }

    public static WebSocketMessage routePurchased(Object routeData, Long gameId, Long playerId) {
        return new WebSocketMessage("ROUTE_PURCHASED", routeData, "Route purchased", gameId, playerId);
    }

    public static WebSocketMessage questionAnswered(Object answerData, Long gameId, Long playerId) {
        return new WebSocketMessage("QUESTION_ANSWERED", answerData, "Question answered", gameId, playerId);
    }

    public static WebSocketMessage gameFinished(Game game) {
        return new WebSocketMessage("GAME_FINISHED", game, "Game finished", game.getId(), null);
    }

    public static WebSocketMessage error(String message, Long gameId) {
        return new WebSocketMessage("ERROR", null, message, gameId, null);
    }
}
