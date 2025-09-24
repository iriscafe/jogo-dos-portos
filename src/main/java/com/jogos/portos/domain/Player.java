package com.jogos.portos.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(nullable = false)
    private BigDecimal money;

    public Player() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public BigDecimal getMoney() { return money; }
    public void setMoney(BigDecimal money) { this.money = money; }
}


