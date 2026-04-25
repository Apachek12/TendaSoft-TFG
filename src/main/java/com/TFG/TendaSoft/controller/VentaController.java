package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.dto.VentaListadoDTO;
import com.TFG.TendaSoft.dto.EstadisticasDTO;
import com.TFG.TendaSoft.model.LineaVenta;
import com.TFG.TendaSoft.model.Venta;
import com.TFG.TendaSoft.service.VentaService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @Data
    public static class VentaRequest {
        private Venta venta;
        private List<LineaVenta> lineas;
    }

    @PostMapping
    public ResponseEntity<?> registrarVenta(@RequestBody VentaRequest peticion) {
        try {
            if (peticion == null || peticion.getVenta() == null || peticion.getLineas() == null) {
                return ResponseEntity.badRequest().body("Error: La venta o las líneas están vacías.");
            }
            Venta nuevaVenta = ventaService.registrarNuevaVenta(peticion.getVenta(), peticion.getLineas());
            return new ResponseEntity<>(nuevaVenta, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/reintentar/{id}")
    public ResponseEntity<?> reintentarVerifactu(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ventaService.reintentarTramiteVerifactu(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<VentaListadoDTO>> obtenerVentas(
            @RequestParam Integer año,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer dia) {
        return ResponseEntity.ok(ventaService.obtenerVentasPorPeriodo(año, mes, dia));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasDTO> obtenerEstadisticas(
            @RequestParam Integer año,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer dia) {
        return ResponseEntity.ok(ventaService.obtenerEstadisticasDePeriodo(año, mes, dia));
    }
}