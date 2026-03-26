package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.model.DatosNegocio;
import com.TFG.TendaSoft.service.DatosNegocioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracion")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DatosNegocioController {

    private final DatosNegocioService datosNegocioService;

    @GetMapping
    public ResponseEntity<DatosNegocio> obtenerConfiguracion() {
        return ResponseEntity.ok(datosNegocioService.obtenerConfiguracion());
    }

    @PutMapping
    public ResponseEntity<DatosNegocio> actualizarConfiguracion(@RequestBody DatosNegocio datos) {
        return ResponseEntity.ok(datosNegocioService.actualizarConfiguracion(datos));
    }
}