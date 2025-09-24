package com.jogos.portos.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "routes")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originPort;

    @Column(nullable = false)
    private String destinationPort;

    @Column(nullable = false)
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Player owner;

    public Route() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOriginPort() { return originPort; }
    public void setOriginPort(String originPort) { this.originPort = originPort; }
    public String getDestinationPort() { return destinationPort; }
    public void setDestinationPort(String destinationPort) { this.destinationPort = destinationPort; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Player getOwner() { return owner; }
    public void setOwner(Player owner) { this.owner = owner; }
}


