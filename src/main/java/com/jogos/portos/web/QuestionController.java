package com.jogos.portos.web;

import com.jogos.portos.domain.Question;
import com.jogos.portos.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public List<Question> list() {
        return questionService.findAll();
    }

    @PostMapping
    public Question create(@RequestBody Question q) {
        return questionService.save(q);
    }

    @GetMapping("/random")
    public ResponseEntity<Question> random() {
        return questionService.randomQuestion()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/answer")
    public Map<String, Object> answer(@RequestBody Map<String, Object> request) {
        Long playerId = Long.valueOf(request.get("playerId").toString());
        Long questionId = Long.valueOf(request.get("questionId").toString());
        String option = request.get("option").toString();
        
        boolean correct = questionService.answerQuestion(playerId, questionId, option);
        return Map.of("correct", correct);
    }
}


