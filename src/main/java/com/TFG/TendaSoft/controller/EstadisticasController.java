package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.dto.EstadisticasDTO;
import com.TFG.TendaSoft.service.VentaService; // Cambiamos al service que tiene la lógica
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EstadisticasController {

    private final VentaService ventaService;

    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasDTO> getEstadisticas(
            @RequestParam Integer año,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer dia) {

        EstadisticasDTO stats = ventaService.obtenerEstadisticasDePeriodo(año, mes, dia);

        return ResponseEntity.ok(stats);
    }
}