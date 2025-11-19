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
                               PortRepository portRepository,
                               RouteRepository routeRepository,
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

            // Ports
            if (portRepository.count() == 0) {
                List<Port> portos = Arrays.asList(
                        new Port("Roterdã"),
                        new Port("Hamburgo"),
                        new Port("Antuérpia"),
                        new Port("Lisboa"),
                        new Port("Barcelona"),
                        new Port("Marselha"),
                        new Port("Gênova"),
                        new Port("Nápoles"),
                        new Port("Pireu"),
                        new Port("Istambul")
                );
                portRepository.saveAll(portos);
                portRepository.flush();
            }

            // Routes
            if (routeRepository.count() == 0) {
                List<Port> portos = portRepository.findAll();
                List<Color> cores = colorRepository.findAll();
                
                // Mapear cores por nome
                Color azul = cores.stream().filter(c -> "Azul".equals(c.getNome())).findFirst().orElse(null);
                Color vermelho = cores.stream().filter(c -> "Vermelho".equals(c.getNome())).findFirst().orElse(null);
                Color verde = cores.stream().filter(c -> "Verde".equals(c.getNome())).findFirst().orElse(null);
                Color amarelo = cores.stream().filter(c -> "Amarelo".equals(c.getNome())).findFirst().orElse(null);
                Color roxo = cores.stream().filter(c -> "Roxo".equals(c.getNome())).findFirst().orElse(null);
                
                // Mapear portos por nome da cidade
                Port roterda = portos.stream().filter(p -> "Roterdã".equals(p.getCidade())).findFirst().orElse(null);
                Port hamburgo = portos.stream().filter(p -> "Hamburgo".equals(p.getCidade())).findFirst().orElse(null);
                Port antuerpia = portos.stream().filter(p -> "Antuérpia".equals(p.getCidade())).findFirst().orElse(null);
                Port lisboa = portos.stream().filter(p -> "Lisboa".equals(p.getCidade())).findFirst().orElse(null);
                Port barcelona = portos.stream().filter(p -> "Barcelona".equals(p.getCidade())).findFirst().orElse(null);
                Port marselha = portos.stream().filter(p -> "Marselha".equals(p.getCidade())).findFirst().orElse(null);
                Port genova = portos.stream().filter(p -> "Gênova".equals(p.getCidade())).findFirst().orElse(null);
                Port napoles = portos.stream().filter(p -> "Nápoles".equals(p.getCidade())).findFirst().orElse(null);
                Port pireu = portos.stream().filter(p -> "Pireu".equals(p.getCidade())).findFirst().orElse(null);
                Port istambul = portos.stream().filter(p -> "Istambul".equals(p.getCidade())).findFirst().orElse(null);
                
                // Criar rotas conforme definido no board.js
                createRoute(routeRepository, roterda, hamburgo, azul, 15.0, 3);
                createRoute(routeRepository, hamburgo, antuerpia, azul, 12.0, 2);
                createRoute(routeRepository, antuerpia, lisboa, azul, 20.0, 4);
                createRoute(routeRepository, lisboa, barcelona, vermelho, 18.0, 3);
                createRoute(routeRepository, barcelona, marselha, vermelho, 15.0, 2);
                createRoute(routeRepository, marselha, genova, vermelho, 12.0, 2);
                createRoute(routeRepository, genova, napoles, verde, 10.0, 2);
                createRoute(routeRepository, napoles, pireu, verde, 25.0, 5);
                createRoute(routeRepository, pireu, istambul, verde, 30.0, 6);
                createRoute(routeRepository, roterda, lisboa, amarelo, 35.0, 7);
                createRoute(routeRepository, hamburgo, barcelona, amarelo, 28.0, 5);
                createRoute(routeRepository, antuerpia, marselha, amarelo, 25.0, 4);
                createRoute(routeRepository, lisboa, genova, roxo, 22.0, 4);
                createRoute(routeRepository, barcelona, napoles, roxo, 20.0, 3);
                createRoute(routeRepository, marselha, pireu, roxo, 18.0, 3);
                
                routeRepository.flush();
            }

            // Question bank
            if (questionBankRepository.count() == 0) {
                QuestionBank bank = new QuestionBank();
                bank.setPontos(10);
                bank.setValor(50.0);
                bank.setGabarito("Banco de perguntas gerais sobre portos e navegação");
                questionBankRepository.save(bank);
            }

            if (questionRepository.count() == 0) {
                QuestionBank bank = questionBankRepository.findAll().get(0);
                Color azul = colorRepository.findAll().stream().filter(c -> "Azul".equals(c.getNome())).findFirst().orElse(null);
                Color vermelho = colorRepository.findAll().stream().filter(c -> "Vermelho".equals(c.getNome())).findFirst().orElse(null);
                Color verde = colorRepository.findAll().stream().filter(c -> "Verde".equals(c.getNome())).findFirst().orElse(null);
                Color amarelo = colorRepository.findAll().stream().filter(c -> "Amarelo".equals(c.getNome())).findFirst().orElse(null);
                Color roxo = colorRepository.findAll().stream().filter(c -> "Roxo".equals(c.getNome())).findFirst().orElse(null);

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

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual é o maior porto da Europa em volume de carga?", azul, bank,
                        new String[]{"Hamburgo","Roterdã","Antuérpia","Marselha"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Em qual país está localizado o porto de Hamburgo?", azul, bank,
                        new String[]{"Bélgica","Alemanha","Países Baixos","Dinamarca"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual canal conecta o Mar Mediterrâneo ao Mar Vermelho?", azul, bank,
                        new String[]{"Canal do Panamá","Canal de Suez","Canal de Kiel","Canal de Corinto"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual é o principal porto do Brasil em volume de carga?", azul, bank,
                        new String[]{"Rio de Janeiro","Santos","Paranaguá","Suape"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual oceano banha a costa oeste da Europa?", azul, bank,
                        new String[]{"Pacífico","Atlântico","Índico","Ártico"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual cidade portuária é a capital da Grécia?", azul, bank,
                        new String[]{"Pireu","Atenas","Salonica","Patras"},
                        new String[]{"A","B","C","D"}, "B");

                // Perguntas VERMELHO (6 novas)
                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual país possui o porto de Antuérpia?", vermelho, bank,
                        new String[]{"Países Baixos","Bélgica","França","Alemanha"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual é o maior porto da França?", vermelho, bank,
                        new String[]{"Le Havre","Marselha","Dunkerque","Calais"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Em qual mar está localizado o porto de Barcelona?", vermelho, bank,
                        new String[]{"Mar Adriático","Mar Mediterrâneo","Mar Negro","Mar Egeu"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual é o principal porto da Espanha?", vermelho, bank,
                        new String[]{"Valência","Barcelona","Algeciras","Bilbao"},
                        new String[]{"A","B","C","D"}, "C");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual cidade portuária italiana é conhecida como berço de Cristóvão Colombo?", vermelho, bank,
                        new String[]{"Roma","Gênova","Veneza","Nápoles"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual é o maior porto do Mar Mediterrâneo?", vermelho, bank,
                        new String[]{"Marselha","Valência","Pireu","Gênova"},
                        new String[]{"A","B","C","D"}, "B");

                // Perguntas VERDE (6 novas)
                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual país possui o porto de Pireu?", verde, bank,
                        new String[]{"Itália","Grécia","Turquia","Chipre"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual cidade portuária é a capital da Turquia?", verde, bank,
                        new String[]{"Istambul","Ancara","Izmir","Antália"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual é o maior porto da Turquia?", verde, bank,
                        new String[]{"Ancara","Istambul","Izmir","Mersin"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual mar separa a Europa da Ásia em Istambul?", verde, bank,
                        new String[]{"Mar Mediterrâneo","Mar Negro","Mar de Mármara","Mar Egeu"},
                        new String[]{"A","B","C","D"}, "C");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual é o principal porto da Itália em volume de carga?", verde, bank,
                        new String[]{"Gênova","Nápoles","Trieste","La Spezia"},
                        new String[]{"A","B","C","D"}, "A");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual cidade portuária grega é famosa por suas ruínas antigas?", verde, bank,
                        new String[]{"Pireu","Atenas","Salonica","Patras"},
                        new String[]{"A","B","C","D"}, "B");

                // Perguntas AMARELO (6 novas)
                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual é o maior porto do mundo em volume de carga?", amarelo, bank,
                        new String[]{"Xangai","Singapura","Roterdã","Los Angeles"},
                        new String[]{"A","B","C","D"}, "A");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual país possui o maior número de portos importantes na Europa?", amarelo, bank,
                        new String[]{"França","Alemanha","Itália","Espanha"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual é a principal função de um porto?", amarelo, bank,
                        new String[]{"Turismo","Comércio e transporte","Pesca","Recreação"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual cidade portuária portuguesa é a capital do país?", amarelo, bank,
                        new String[]{"Porto","Lisboa","Setúbal","Aveiro"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual é o maior porto da África?", amarelo, bank,
                        new String[]{"Cidade do Cabo","Durban","Port Said","Tanger"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual instrumento é essencial para navegação marítima?", amarelo, bank,
                        new String[]{"Bússola","Telescópio","Binóculo","Rádio"},
                        new String[]{"A","B","C","D"}, "A");

                // Perguntas ROXO (6 novas)
                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual é o menor oceano do mundo?", roxo, bank,
                        new String[]{"Atlântico","Pacífico","Índico","Ártico"},
                        new String[]{"A","B","C","D"}, "D");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual cidade portuária é conhecida como \"Pérola do Mediterrâneo\"?", roxo, bank,
                        new String[]{"Barcelona","Marselha","Nápoles","Valência"},
                        new String[]{"A","B","C","D"}, "C");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual é o principal tipo de carga transportada em portos?", roxo, bank,
                        new String[]{"Passageiros","Contêineres","Petróleo","Carga geral"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual país possui o porto de Lisboa?", roxo, bank,
                        new String[]{"Espanha","Portugal","França","Itália"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual é o nome do estreito que conecta o Mar Mediterrâneo ao Oceano Atlântico?", roxo, bank,
                        new String[]{"Estreito de Bósforo","Estreito de Gibraltar","Estreito de Dardanelos","Canal da Mancha"},
                        new String[]{"A","B","C","D"}, "B");

                createQuestionWithAlternatives(questionRepository, alternativeRepository, jdbcTemplate,
                        "Qual cidade portuária italiana é famosa por seus canais?", roxo, bank,
                        new String[]{"Roma","Veneza","Gênova","Nápoles"},
                        new String[]{"A","B","C","D"}, "B");
            }
        };
    }

    private static void createRoute(RouteRepository routeRepository,
                                    Port portoOrigem,
                                    Port portoDestino,
                                    Color cor,
                                    Double custo,
                                    Integer pontos) {
        Route route = new Route();
        route.setPortoOrigem(portoOrigem);
        route.setPortoDestino(portoDestino);
        route.setCor(cor);
        route.setCusto(custo);
        route.setPontos(pontos);
        routeRepository.save(route);
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
        alternativeRepository.flush(); 
        
        if (correctId != null) {
            jdbcTemplate.update("UPDATE questions SET resposta_correta_id = ? WHERE id = ?", 
                               correctId, saved.getId());
        }
    }
}
