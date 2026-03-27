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
@CrossOrigin(origins = "*")
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
        // El JSON debe incluir el ID del usuario que quiere cerrar
        private Integer idUsuario;
        private BigDecimal dineroFisicoContado;
    }

    @PostMapping("/abrir")
    public ResponseEntity<CierreCaja> abrirCaja(@RequestBody AperturaRequest peticion) {
        // No deja abrir si ya hay una caja abierta
        CierreCaja caja = cierreCajaService.abrirCaja(peticion.getFondoInicial(), peticion.getUsuario());
        return new ResponseEntity<>(caja, HttpStatus.CREATED);
    }

    @PostMapping("/cerrar") // Quitamos el /{idCierre} de aquí
    public ResponseEntity<CierreCaja> cerrarCaja(@RequestBody CierreRequest peticion) {
        CierreCaja cajaCerrada = cierreCajaService.cerrarCaja(
                peticion.getIdUsuario(),
                peticion.getDineroFisicoContado()
        );
        return ResponseEntity.ok(cajaCerrada);
    }
}