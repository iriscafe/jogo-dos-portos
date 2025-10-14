package com.jogos.portos.service;

import com.jogos.portos.domain.Color;
import com.jogos.portos.repository.ColorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ColorService {

    @Autowired
    private ColorRepository colorRepository;

    public List<Color> findAll() {
        return colorRepository.findAll();
    }

    public Optional<Color> findById(Long id) {
        return colorRepository.findById(id);
    }

    public Color save(Color color) {
        return colorRepository.save(color);
    }

    public void deleteById(Long id) {
        colorRepository.deleteById(id);
    }

    public Optional<Color> findByNome(String nome) {
        return colorRepository.findAll().stream()
                .filter(color -> color.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }
}
