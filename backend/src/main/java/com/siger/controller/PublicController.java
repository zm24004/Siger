package com.siger.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.siger.dto.EventoResponse;
import com.siger.dto.PageResponse;
import com.siger.service.EventoService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/public")
@AllArgsConstructor 
public class PublicController {

    private final EventoService eventoService;

    @GetMapping ("/eventos-populares")
    public PageResponse<EventoResponse> eventosPopulares(@RequestParam Integer page, @RequestParam Integer limit) {
        return eventoService.eventosPopulares(page, limit);
    }
    
}
