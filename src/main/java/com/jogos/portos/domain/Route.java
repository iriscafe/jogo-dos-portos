package com.jogos.portos.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "routes")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "porto_origem_id")
    private Port portoOrigem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "porto_destino_id")
    private Port portoDestino;

    @Column(nullable = false)
    private Double custo;

    @Column(nullable = false)
    private Integer pontos;

    @ManyToOne
    @JoinColumn(name = "dono_id")
    private Player dono;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cor_id")
    private Color cor;

    @ManyToOne
    @JoinColumn(name = "game_id")
    @JsonBackReference
    private Game game;

    public Route() {}

    public void atribuirDono(Player jogador) {
        this.dono = jogador;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Port getPortoOrigem() { return portoOrigem; }
    public void setPortoOrigem(Port portoOrigem) { this.portoOrigem = portoOrigem; }
    public Port getPortoDestino() { return portoDestino; }
    public void setPortoDestino(Port portoDestino) { this.portoDestino = portoDestino; }
    public Double getCusto() { return custo; }
    public void setCusto(Double custo) { this.custo = custo; }
    public Integer getPontos() { return pontos; }
    public void setPontos(Integer pontos) { this.pontos = pontos; }
    public Player getDono() { return dono; }
    public void setDono(Player dono) { this.dono = dono; }
    public Color getCor() { return cor; }
    public void setCor(Color cor) { this.cor = cor; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
}


