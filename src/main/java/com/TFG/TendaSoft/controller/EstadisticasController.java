package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.dto.EstadisticasDTO;
import com.TFG.TendaSoft.service.EstadisticasService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EstadisticasController {

    private final EstadisticasService reporteService;

    @GetMapping("/resumen")
    public EstadisticasDTO getResumen() {
        return reporteService.obtenerResumenFinanciero();
    }
}