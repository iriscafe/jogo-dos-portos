package com.jogos.portos.service;

import com.jogos.portos.domain.Alternative;
import com.jogos.portos.repository.AlternativeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlternativeService {

    @Autowired
    private AlternativeRepository alternativeRepository;

    public List<Alternative> findAll() {
        return alternativeRepository.findAll();
    }

    public Optional<Alternative> findById(Long id) {
        return alternativeRepository.findById(id);
    }

    public Alternative save(Alternative alternative) {
        return alternativeRepository.save(alternative);
    }

    public void deleteById(Long id) {
        alternativeRepository.deleteById(id);
    }

    public List<Alternative> findByQuestionId(Long questionId) {
        return alternativeRepository.findAll().stream()
                .filter(alt -> alt.getQuestion() != null && alt.getQuestion().getId().equals(questionId))
                .toList();
    }
}
