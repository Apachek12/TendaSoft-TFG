package com.TFG.TendaSoft.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerifactuUtils {
    private static final Logger logger = LoggerFactory.getLogger(VerifactuUtils.class);
    // Formato de fecha exacto que usa la AEAT
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static String generarHashVerifactu(String nifEmisor, String numFactura,
                                              LocalDateTime fechaExpedicion, String tipoFactura,
                                              BigDecimal cuotaTotal, BigDecimal importeTotal,
                                              String hashAnterior, String fechaHoraHuso) {

        // Para la primera factura
        String huellaAnterior = (hashAnterior == null || hashAnterior.equals("INICIO-SISTEMA"))
                ? "" : hashAnterior;

        String cadena =
                "IDEmisorFactura=" + nifEmisor +
                        "&NumSerieFactura=" + numFactura +
                        "&FechaExpedicionFactura=" + fechaExpedicion.format(FORMATO_FECHA) +
                        "&TipoFactura=" + tipoFactura +
                        "&CuotaTotal=" + formatearImporte(cuotaTotal) +
                        "&ImporteTotal=" + formatearImporte(importeTotal) +
                        "&Huella=" + huellaAnterior +
                        "&FechaHoraHusoGenRegistro=" + fechaHoraHuso;

        logger.info("Cadena normalizada: {}", cadena);
        return calcularSHA256(cadena);
    }

    private static String formatearImporte(BigDecimal importe) {
        return importe.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String calcularSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular SHA-256", e);
        }
    }
}