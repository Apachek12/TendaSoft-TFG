package com.TFG.TendaSoft.controller;

import com.TFG.TendaSoft.model.DatosNegocio;
import com.TFG.TendaSoft.service.DatosNegocioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> guardarConfiguracion(
            @RequestParam("nombreEmpresa") String nombreEmpresa,
            @RequestParam("cif") String cif,
            @RequestParam("direccion") String direccion,
            @RequestParam(value = "mensajeTicket", required = false) String mensajeTicket,
            @RequestParam("verifactuActivado") Boolean verifactuActivado,
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            @RequestParam(value = "certificado", required = false) MultipartFile certificado) { // <-- AÑADIDO EL CERTIFICADO

        try {
            // Actualizamos la llamada al servicio para enviar también el certificado.
            DatosNegocio guardado = datosNegocioService.guardarConfiguracion(
                    nombreEmpresa, cif, direccion, mensajeTicket, verifactuActivado, logo, certificado);

            return ResponseEntity.ok(guardado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar la configuración: " + e.getMessage());
        }
    }
}