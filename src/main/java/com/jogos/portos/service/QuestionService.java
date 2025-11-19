package com.jogos.portos.service;

import com.jogos.portos.domain.Game;
import com.jogos.portos.domain.Player;
import com.jogos.portos.domain.Question;
import com.jogos.portos.repository.GameRepository;
import com.jogos.portos.repository.PlayerRepository;
import com.jogos.portos.repository.QuestionRepository;
import com.jogos.portos.web.dto.WebSocketMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameRepository gameRepository;

    public QuestionService(QuestionRepository questionRepository, PlayerRepository playerRepository, SimpMessagingTemplate messagingTemplate, GameRepository gameRepository) {
        this.questionRepository = questionRepository;
        this.playerRepository = playerRepository;
        this.messagingTemplate = messagingTemplate;
        this.gameRepository = gameRepository;
    }

    private static final Double REWARD_CORRECT = 20.0;
    private static final Double PENALTY_WRONG = 5.0;

    @Transactional(readOnly = true)
    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    @Transactional
    public Question save(Question q) {
        return questionRepository.save(q);
    }

    @Transactional(readOnly = true)
    public Optional<Question> randomQuestion() {
        List<Question> all = questionRepository.findAll();
        if (all.isEmpty()) return Optional.empty();
        Random r = new Random();
        return Optional.of(all.get(r.nextInt(all.size())));
    }

    @Transactional
    public boolean answerQuestion(Long playerId, Long questionId, Long alternativeId) {
        try {
            Question q = questionRepository.findById(questionId)
                    .orElseThrow(() -> new IllegalArgumentException("Pergunta não encontrada"));
            Player p = playerRepository.findById(playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado"));

            // Verificar se a alternativa escolhida é a correta
            boolean correct = q.getRespostaCorreta().getId().equals(alternativeId);

            if (correct) {
                p.setDinheiro(p.getDinheiro() + REWARD_CORRECT);
            } else {
                p.setDinheiro(p.getDinheiro() - PENALTY_WRONG);
            }
            playerRepository.save(p);
            
            // Notificar via WebSocket sobre a resposta
            if (p.getGame() != null) {
                Long gameId = p.getGame().getId();
                messagingTemplate.convertAndSend("/topic/game/" + gameId, 
                    WebSocketMessage.questionAnswered(Map.of(
                        "correct", correct,
                        "playerId", playerId,
                        "questionId", questionId,
                        "alternativeId", alternativeId,
                        "reward", correct ? REWARD_CORRECT : -PENALTY_WRONG
                    ), gameId, playerId));
                
                // Atualizar jogo completo para sincronizar dinheiro em tempo real
                Game game = gameRepository.findById(gameId).orElse(null);
                if (game != null) {
                    messagingTemplate.convertAndSend("/topic/game/" + gameId, 
                        WebSocketMessage.gameUpdate(game));
                }
            }
            
            return correct;
        } catch (Exception e) {
            System.err.println("Erro ao responder pergunta: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}


