package com.jogos.portos.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ports")
public class Port {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cidade;

    @OneToMany(mappedBy = "portoOrigem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Route> rotasOrigem = new ArrayList<>();

    @OneToMany(mappedBy = "portoDestino", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Route> rotasDestino = new ArrayList<>();

    public Port() {}

    public Port(String cidade) {
        this.cidade = cidade;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public List<Route> getRotasOrigem() {
        return rotasOrigem;
    }

    public void setRotasOrigem(List<Route> rotasOrigem) {
        this.rotasOrigem = rotasOrigem;
    }

    public List<Route> getRotasDestino() {
        return rotasDestino;
    }

    public void setRotasDestino(List<Route> rotasDestino) {
        this.rotasDestino = rotasDestino;
    }
}
