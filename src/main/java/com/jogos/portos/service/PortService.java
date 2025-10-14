package com.jogos.portos.service;

import com.jogos.portos.domain.Port;
import com.jogos.portos.repository.PortRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PortService {

    @Autowired
    private PortRepository portRepository;

    public List<Port> findAll() {
        return portRepository.findAll();
    }

    public Optional<Port> findById(Long id) {
        return portRepository.findById(id);
    }

    public Port save(Port port) {
        return portRepository.save(port);
    }

    public void deleteById(Long id) {
        portRepository.deleteById(id);
    }

    public List<Port> findByCidade(String cidade) {
        return portRepository.findAll().stream()
                .filter(port -> port.getCidade().equalsIgnoreCase(cidade))
                .toList();
    }
}
