package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.model.CierreCaja;
import com.TFG.TendaSoft.model.Usuario;
import com.TFG.TendaSoft.service.CierreCajaService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/caja")
@RequiredArgsConstructor
public class CierreCajaController {

    private final CierreCajaService cierreCajaService;

    @Data
    public static class AperturaRequest {
        private BigDecimal fondoInicial;
        private Usuario usuario;
    }

    @Data
    public static class CierreRequest {
        private Integer idUsuario;
        private BigDecimal dineroFisicoContado;
    }

    @PostMapping("/abrir")
    public ResponseEntity<CierreCaja> abrirCaja(@RequestBody AperturaRequest peticion) {
        CierreCaja caja = cierreCajaService.abrirCaja(peticion.getFondoInicial(), peticion.getUsuario());
        return new ResponseEntity<>(caja, HttpStatus.CREATED);
    }

    @PostMapping("/cerrar")
    public ResponseEntity<CierreCaja> cerrarCaja(@RequestBody CierreRequest peticion) {
        CierreCaja cajaCerrada = cierreCajaService.cerrarCaja(
                peticion.getIdUsuario(),
                peticion.getDineroFisicoContado()
        );
        return ResponseEntity.ok(cajaCerrada);
    }

    @GetMapping("/estado/{idUsuario}")
    public ResponseEntity<CierreCaja> obtenerEstado(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(cierreCajaService.obtenerCajaAbierta(idUsuario));
    }

    @GetMapping("/resumen/{idUsuario}")
    public ResponseEntity<?> obtenerResumen(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(cierreCajaService.obtenerResumenActual(idUsuario));
    }
}