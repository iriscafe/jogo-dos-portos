package com.jogos.portos.web;

import com.jogos.portos.domain.Game;
import com.jogos.portos.domain.Player;
import com.jogos.portos.domain.Question;
import com.jogos.portos.service.GameService;
import com.jogos.portos.service.QuestionService;
import com.jogos.portos.web.dto.WebSocketMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;
    private final QuestionService questionService;

    public WebSocketController(SimpMessagingTemplate messagingTemplate, 
                             GameService gameService, 
                             QuestionService questionService) {
        this.messagingTemplate = messagingTemplate;
        this.gameService = gameService;
        this.questionService = questionService;
    }

    @MessageMapping("/game/join")
    public WebSocketMessage joinGame(Map<String, Object> payload) {
        try {
            Long gameId = Long.valueOf(payload.get("gameId").toString());
            String playerName = payload.get("playerName").toString();
            
            Player player = new Player();
            player.setName(playerName);
            
            Player joinedPlayer = gameService.joinGame(gameId, player);
            Game game = gameService.findById(gameId).orElse(null);
            
            if (game != null) {
                // Notificar todos os jogadores do jogo
                messagingTemplate.convertAndSend("/topic/game/" + gameId, 
                    WebSocketMessage.playerJoined(joinedPlayer, game));
                return WebSocketMessage.gameUpdate(game);
            }
            
            return WebSocketMessage.error("Game not found", gameId);
        } catch (Exception e) {
            return WebSocketMessage.error("Error joining game: " + e.getMessage(), null);
        }
    }

    @MessageMapping("/game/next-turn")
    public WebSocketMessage nextTurn(Map<String, Object> payload) {
        try {
            Long gameId = Long.valueOf(payload.get("gameId").toString());
            
            Game game = gameService.nextTurn(gameId);
            
            // Notificar todos os jogadores do jogo
            messagingTemplate.convertAndSend("/topic/game/" + gameId, 
                WebSocketMessage.turnChanged(game));
            
            return WebSocketMessage.gameUpdate(game);
        } catch (Exception e) {
            return WebSocketMessage.error("Error changing turn: " + e.getMessage(), null);
        }
    }

    @MessageMapping("/game/answer-question")
    public WebSocketMessage answerQuestion(Map<String, Object> payload) {
        try {
            Long gameId = Long.valueOf(payload.get("gameId").toString());
            Long playerId = Long.valueOf(payload.get("playerId").toString());
            Long questionId = Long.valueOf(payload.get("questionId").toString());
            Long alternativeId = Long.valueOf(payload.get("alternativeId").toString());
            
            boolean correct = questionService.answerQuestion(playerId, questionId, alternativeId);
            
            Map<String, Object> answerData = Map.of(
                "correct", correct,
                "playerId", playerId,
                "questionId", questionId,
                "alternativeId", alternativeId
            );
            
            // Notificar todos os jogadores do resultado
            messagingTemplate.convertAndSend("/topic/game/" + gameId, 
                WebSocketMessage.questionAnswered(answerData, gameId, playerId));
            
            return WebSocketMessage.questionAnswered(answerData, gameId, playerId);
        } catch (Exception e) {
            return WebSocketMessage.error("Error answering question: " + e.getMessage(), null);
        }
    }

    @MessageMapping("/game/finish")
    public WebSocketMessage finishGame(Map<String, Object> payload) {
        try {
            Long gameId = Long.valueOf(payload.get("gameId").toString());
            
            Game game = gameService.finish(gameId);
            
            // Notificar todos os jogadores que o jogo terminou
            messagingTemplate.convertAndSend("/topic/game/" + gameId, 
                WebSocketMessage.gameFinished(game));
            
            return WebSocketMessage.gameFinished(game);
        } catch (Exception e) {
            return WebSocketMessage.error("Error finishing game: " + e.getMessage(), null);
        }
    }

    @MessageMapping("/game/restart")
    public WebSocketMessage restartGame(Map<String, Object> payload) {
        try {
            Long gameId = Long.valueOf(payload.get("gameId").toString());
            
            Game game = gameService.restart(gameId);
            
            // Notificar todos os jogadores que o jogo foi reiniciado
            messagingTemplate.convertAndSend("/topic/game/" + gameId, 
                WebSocketMessage.gameUpdate(game));
            
            return WebSocketMessage.gameUpdate(game);
        } catch (Exception e) {
            return WebSocketMessage.error("Error restarting game: " + e.getMessage(), null);
        }
    }

    @MessageMapping("/game/get-random-question")
    public WebSocketMessage getRandomQuestion(Map<String, Object> payload) {
        try {
            Long gameId = Long.valueOf(payload.get("gameId").toString());
            
            Question question = questionService.randomQuestion().orElse(null);
            
            if (question != null) {
                Map<String, Object> questionData = Map.of(
                    "question", question,
                    "gameId", gameId
                );
                
                // Enviar pergunta para todos os jogadores do jogo
                messagingTemplate.convertAndSend("/topic/game/" + gameId, 
                    new WebSocketMessage("NEW_QUESTION", questionData, "New question available", gameId, null));
                
                return new WebSocketMessage("NEW_QUESTION", questionData, "New question available", gameId, null);
            }
            
            return WebSocketMessage.error("No questions available", gameId);
        } catch (Exception e) {
            return WebSocketMessage.error("Error getting question: " + e.getMessage(), null);
        }
    }
}
