package com.jogos.portos.service;

import com.jogos.portos.domain.Player;
import com.jogos.portos.domain.Question;
import com.jogos.portos.repository.PlayerRepository;
import com.jogos.portos.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    private static final BigDecimal REWARD_CORRECT = new BigDecimal("20.00");
    private static final BigDecimal PENALTY_WRONG = new BigDecimal("5.00");

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
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Pergunta não encontrada"));
        Player p = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado"));

        boolean correct = q.getCorrectOption().equalsIgnoreCase(option);
        if (correct) {
            p.setMoney(p.getMoney().add(REWARD_CORRECT));
        } else {
            p.setMoney(p.getMoney().subtract(PENALTY_WRONG));
        }
        playerRepository.save(p);
        return correct;
    }
}


