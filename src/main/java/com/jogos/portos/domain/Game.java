package com.jogos.portos.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Route> rotas = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> perguntas = new ArrayList<>();

    public Game() {
        this.status = GameStatus.CRIADO;
    }

    public void iniciarPartida() {
        this.status = GameStatus.EM_ANDAMENTO;
    }

    public void encerrarPartida() {
        this.status = GameStatus.FINALIZADO;
    }

    public void registrarJogada() {
        // Implementar lógica de registro de jogada
        // Por enquanto, apenas placeholder
    }

    public Map<Player, Integer> calcularPontuacaoFinal() {
        Map<Player, Integer> pontuacoes = new HashMap<>();
        for (Player player : players) {
            int pontos = 0;
            // Calcular pontos baseado nas rotas possuídas
            for (Route rota : rotas) {
                if (rota.getDono() != null && rota.getDono().equals(player)) {
                    pontos += rota.getPontos();
                }
            }
            pontuacoes.put(player, pontos);
        }
        return pontuacoes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }
    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }
    public List<Route> getRotas() { return rotas; }
    public void setRotas(List<Route> rotas) { this.rotas = rotas; }
    public List<Question> getPerguntas() { return perguntas; }
    public void setPerguntas(List<Question> perguntas) { this.perguntas = perguntas; }
}


