package com.TFG.TendaSoft.controller;

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
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    // Esta clase interna nos sirve para recibir un JSON complejo desde React
    @Data
    public static class VentaRequest {
        private Venta venta;
        private List<LineaVenta> lineas;
    }

    @PostMapping
    public ResponseEntity<Venta> registrarVenta(@RequestBody VentaRequest peticion) {
        Venta nuevaVenta = ventaService.registrarNuevaVenta(peticion.getVenta(), peticion.getLineas());
        return new ResponseEntity<>(nuevaVenta, HttpStatus.CREATED);
    }
}