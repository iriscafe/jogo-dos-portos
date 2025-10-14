package com.jogos.portos.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Repositories {

    @Autowired
    public GameRepository gameRepository;

    @Autowired
    public PlayerRepository playerRepository;

    @Autowired
    public QuestionRepository questionRepository;

    @Autowired
    public RouteRepository routeRepository;

    @Autowired
    public PortRepository portRepository;

    @Autowired
    public ColorRepository colorRepository;

    @Autowired
    public AlternativeRepository alternativeRepository;

    @Autowired
    public QuestionBankRepository questionBankRepository;
}
