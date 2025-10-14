package com.jogos.portos.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question_banks")
public class QuestionBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "bancoPerguntas", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> perguntas = new ArrayList<>();

    @Column(nullable = false)
    private Integer pontos;

    @Column(nullable = false)
    private Double valor;

    @Column(columnDefinition = "TEXT")
    private String gabarito;

    public QuestionBank() {}

    public QuestionBank(Integer pontos, Double valor) {
        this.pontos = pontos;
        this.valor = valor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Question> getPerguntas() {
        return perguntas;
    }

    public void setPerguntas(List<Question> perguntas) {
        this.perguntas = perguntas;
    }

    public Integer getPontos() {
        return pontos;
    }

    public void setPontos(Integer pontos) {
        this.pontos = pontos;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getGabarito() {
        return gabarito;
    }

    public void setGabarito(String gabarito) {
        this.gabarito = gabarito;
    }
}
