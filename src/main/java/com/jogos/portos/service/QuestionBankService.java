package com.jogos.portos.service;

import com.jogos.portos.domain.QuestionBank;
import com.jogos.portos.repository.QuestionBankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionBankService {

    @Autowired
    private QuestionBankRepository questionBankRepository;

    public List<QuestionBank> findAll() {
        return questionBankRepository.findAll();
    }

    public Optional<QuestionBank> findById(Long id) {
        return questionBankRepository.findById(id);
    }

    public QuestionBank save(QuestionBank questionBank) {
        return questionBankRepository.save(questionBank);
    }

    public void deleteById(Long id) {
        questionBankRepository.deleteById(id);
    }
}
