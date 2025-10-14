package com.jogos.portos.web;

import com.jogos.portos.domain.Color;
import com.jogos.portos.service.ColorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colors")
public class ColorController {

    private final ColorService colorService;

    public ColorController(ColorService colorService) {
        this.colorService = colorService;
    }

    @GetMapping
    public List<Color> list() {
        return colorService.findAll();
    }

    @PostMapping
    public Color save(@RequestBody Color color) {
        return colorService.save(color);
    }
}
