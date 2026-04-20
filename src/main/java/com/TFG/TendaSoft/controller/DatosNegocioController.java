package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.model.DatosNegocio;
import com.TFG.TendaSoft.service.DatosNegocioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.TFG.TendaSoft.repository.DatosNegocioRepository;

@RestController
@RequestMapping("/api/configuracion")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DatosNegocioController {

    private final DatosNegocioService datosNegocioService;
    private final DatosNegocioRepository datosNegocioRepository;

    @GetMapping
    public ResponseEntity<DatosNegocio> obtenerConfiguracion() {
        return ResponseEntity.ok(datosNegocioService.obtenerConfiguracion());
    }

    @PostMapping
    public ResponseEntity<?> crearDatosNegocio(@RequestBody DatosNegocio datosNegocio) {
        // Forzamos el ID a 1 porque normalmente solo hay 1 empresa usando el TPV
        datosNegocio.setId(1);
        DatosNegocio guardado = datosNegocioRepository.save(datosNegocio);
        return ResponseEntity.ok(guardado);
    }

    @PutMapping
    public ResponseEntity<DatosNegocio> actualizarConfiguracion(@RequestBody DatosNegocio datos) {
        return ResponseEntity.ok(datosNegocioService.actualizarConfiguracion(datos));
    }
}