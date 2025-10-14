package com.jogos.portos.service;

import com.jogos.portos.domain.Player;
import com.jogos.portos.domain.Question;
import com.jogos.portos.repository.PlayerRepository;
import com.jogos.portos.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final PlayerRepository playerRepository;

    public QuestionService(QuestionRepository questionRepository, PlayerRepository playerRepository) {
        this.questionRepository = questionRepository;
        this.playerRepository = playerRepository;
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
    public boolean answerQuestion(Long playerId, Long questionId, String option) {
        try {
            Question q = questionRepository.findById(questionId)
                    .orElseThrow(() -> new IllegalArgumentException("Pergunta não encontrada"));
            Player p = playerRepository.findById(playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado"));

            // Para compatibilidade com a estrutura antiga, vamos verificar se há resposta correta
            boolean correct = false;
            if (q.getRespostaCorreta() != null) {
                correct = q.getRespostaCorreta().getLetra().equalsIgnoreCase(option);
            }
            
            if (correct) {
                p.setDinheiro(p.getDinheiro() + REWARD_CORRECT);
            } else {
                p.setDinheiro(p.getDinheiro() - PENALTY_WRONG);
            }
            playerRepository.save(p);
            return correct;
        } catch (Exception e) {
            System.err.println("Erro ao responder pergunta: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}


