package com.jogos.portos.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "game_id")
    @JsonBackReference
    private Game game;

    @Column(nullable = false)
    private Double dinheiro;

    @Column(nullable = false)
    private Integer naviosDisponiveis;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cor_id")
    private Color cor;

    @ElementCollection
    @CollectionTable(name = "player_tokens", joinColumns = @JoinColumn(name = "player_id"))
    @Column(name = "token_count")
    private List<Integer> tokensColoridos = new ArrayList<>();

    @OneToMany(mappedBy = "dono", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Route> rotasPossuidas = new ArrayList<>();

    public Player() {}

    public void receberDinheiro(Double valor) {
        this.dinheiro = this.dinheiro + valor;
    }

    public boolean responderPergunta(Question pergunta, Alternative resposta) {
        return pergunta.validarResposta(resposta);
    }

    public void comprarRota(Route rota) {
        if (this.dinheiro >= rota.getCusto()) {
            this.dinheiro = this.dinheiro - rota.getCusto();
            rota.atribuirDono(this);
            this.rotasPossuidas.add(rota);
        }
    }

    public void venderRota(Route rota, Player comprador) {
        if (this.rotasPossuidas.contains(rota)) {
            comprador.comprarRota(rota);
            this.rotasPossuidas.remove(rota);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public Double getDinheiro() { return dinheiro; }
    public void setDinheiro(Double dinheiro) { this.dinheiro = dinheiro; }
    public Integer getNaviosDisponiveis() { return naviosDisponiveis; }
    public void setNaviosDisponiveis(Integer naviosDisponiveis) { this.naviosDisponiveis = naviosDisponiveis; }
    public Color getCor() { return cor; }
    public void setCor(Color cor) { this.cor = cor; }
    public List<Integer> getTokensColoridos() { return tokensColoridos; }
    public void setTokensColoridos(List<Integer> tokensColoridos) { this.tokensColoridos = tokensColoridos; }
    public List<Route> getRotasPossuidas() { return rotasPossuidas; }
    public void setRotasPossuidas(List<Route> rotasPossuidas) { this.rotasPossuidas = rotasPossuidas; }
}


