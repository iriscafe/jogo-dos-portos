package com.jogos.portos.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.List;
import java.util.Random;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String enunciado;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alternative> alternativas;

    @ManyToOne(optional = true)
    @JoinColumn(name = "resposta_correta_id", nullable = true)
    private Alternative respostaCorreta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cor_id")
    private Color cor;

    @ManyToOne
    @JoinColumn(name = "banco_perguntas_id")
    @JsonBackReference
    private QuestionBank bancoPerguntas;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    public Question() {}

    public boolean validarResposta(Alternative resposta) {
        return this.respostaCorreta.equals(resposta);
    }

    public static Color sortearCor(List<Color> coresDisponiveis) {
        if (coresDisponiveis == null || coresDisponiveis.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return coresDisponiveis.get(random.nextInt(coresDisponiveis.size()));
    }

    public static Question sortearPergunta(List<Question> perguntasDisponiveis) {
        if (perguntasDisponiveis == null || perguntasDisponiveis.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return perguntasDisponiveis.get(random.nextInt(perguntasDisponiveis.size()));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }
    public List<Alternative> getAlternativas() { return alternativas; }
    public void setAlternativas(List<Alternative> alternativas) { this.alternativas = alternativas; }
    public Alternative getRespostaCorreta() { return respostaCorreta; }
    public void setRespostaCorreta(Alternative respostaCorreta) { this.respostaCorreta = respostaCorreta; }
    public Color getCor() { return cor; }
    public void setCor(Color cor) { this.cor = cor; }
    public QuestionBank getBancoPerguntas() { return bancoPerguntas; }
    public void setBancoPerguntas(QuestionBank bancoPerguntas) { this.bancoPerguntas = bancoPerguntas; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
}


