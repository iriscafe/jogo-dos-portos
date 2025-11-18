package com.jogos.portos;

import com.jogos.portos.domain.*;
import com.jogos.portos.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    @Transactional
    CommandLineRunner initData(ColorRepository colorRepository,
                               QuestionBankRepository questionBankRepository,
                               QuestionRepository questionRepository,
                               AlternativeRepository alternativeRepository,
                               JdbcTemplate jdbcTemplate) {
        return args -> {
            // Colors
            if (colorRepository.count() == 0) {
                List<Color> cores = Arrays.asList(
                        new Color("Azul"),
                        new Color("Vermelho"),
                        new Color("Verde"),
                        new Color("Amarelo"),
                        new Color("Roxo")
                );
                colorRepository.saveAll(cores);
            }

            // Question bank
            if (questionBankRepository.count() == 0) {
                QuestionBank bank = new QuestionBank();
                bank.setPontos(10);
                bank.setValor(50.0);
                bank.setGabarito("Banco de perguntas gerais sobre portos e navegação");
                questionBankRepository.save(bank);
            }

            // Questions with alternatives
            if (questionRepository.count() == 0) {
                QuestionBank bank = questionBankRepository.findAll().get(0);
                Color azul = colorRepository.findAll().stream().filter(c -> "Azul".equals(c.getNome())).findFirst().orElse(null);
                Color vermelho = colorRepository.findAll().stream().filter(c -> "Vermelho".equals(c.getNome())).findFirst().orElse(null);
                Color verde = colorRepository.findAll().stream().filter(c -> "Verde".equals(c.getNome())).findFirst().orElse(null);
                Color amarelo = colorRepository.findAll().stream().filter(c -> "Amarelo".equals(c.getNome())).findFirst().orElse(null);
                Color roxo = colorRepository.findAll().stream().filter(c -> "Roxo".equals(c.getNome())).findFirst().orElse(null);

                // Helper to create question + alternatives
                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual é a capital do Brasil?", azul, bank,
                        new String[]{"São Paulo","Brasília","Rio de Janeiro","Salvador"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual o maior oceano do planeta?", vermelho, bank,
                        new String[]{"Atlântico","Pacífico","Índico","Ártico"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual país possui o porto de Roterdã?", verde, bank,
                        new String[]{"Alemanha","Bélgica","Países Baixos","Dinamarca"},
                        new String[]{"A","B","C","D"}, "C");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual é a moeda oficial da Alemanha?", amarelo, bank,
                        new String[]{"Marco","Euro","Libra","Dólar"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual cidade é conhecida como \"Cidade Luz\"?", roxo, bank,
                        new String[]{"Londres","Paris","Roma","Madrid"},
                        new String[]{"A","B","C","D"}, "B");
            }
        };
    }

    private static void createQuestionWithAlternatives(QuestionRepository questionRepository,
                                                       AlternativeRepository alternativeRepository,
                                                       JdbcTemplate jdbcTemplate,
                                                       String enunciado,
                                                       Color cor,
                                                       QuestionBank bank,
                                                       String[] textos,
                                                       String[] letras,
                                                       String correta) {
        Question q = new Question();
        q.setEnunciado(enunciado);
        q.setCor(cor);
        q.setBancoPerguntas(bank);
        Question saved = questionRepository.save(q);
        questionRepository.flush(); // força persistência

        Long correctId = null;
        for (int i = 0; i < textos.length; i++) {
            Alternative alt = new Alternative();
            alt.setTexto(textos[i]);
            alt.setLetra(letras[i]);
            alt.setQuestion(saved);
            Alternative savedAlt = alternativeRepository.save(alt);
            if (letras[i].equals(correta)) {
                correctId = savedAlt.getId();
            }
        }
        alternativeRepository.flush(); // força persistência das alternativas
        
        // Atualiza resposta correta via SQL direto (evita problemas de referência)
        if (correctId != null) {
            jdbcTemplate.update("UPDATE questions SET resposta_correta_id = ? WHERE id = ?", 
                               correctId, saved.getId());
        }
    }
}
