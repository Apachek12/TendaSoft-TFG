package com.TFG.TendaSoft.utils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class VerifactuUtils {

    public static String generarHashVerifactu(String nifEmisor, String numFactura,
                                              LocalDateTime fechaExpedicion, String tipoFactura,
                                              BigDecimal cuotaTotal, BigDecimal importeTotal,
                                              String hashAnterior, String fechaHoraHusoGenRegistro) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String fechaFormateada = fechaExpedicion.format(formatter);

        // La AEAT concatena con & y usa los valores tal como aparecen en el XML.
        // Para la primera factura, Huella= (cadena vacía, no "INICIO-SISTEMA").
        // Los importes se formatean igual que en el XML: con punto decimal y 2 decimales.
        String huellaAnteriorSafe = (hashAnterior == null || hashAnterior.equals("INICIO-SISTEMA"))
                ? "" : hashAnterior;

        // Formato exacto confirmado por la AEAT en el error 2000:
        // IDEmisorFactura=X&NumSerieFactura=X&FechaExpedicionFactura=X&TipoFactura=X
        // &CuotaTotal=X&ImporteTotal=X&Huella=X&FechaHoraHusoGenRegistro=X
        String cadenaAFirmar =
                "IDEmisorFactura=" + nifEmisor +
                        "&NumSerieFactura=" + numFactura +
                        "&FechaExpedicionFactura=" + fechaFormateada +
                        "&TipoFactura=" + tipoFactura +
                        "&CuotaTotal=" + formatearImporte(cuotaTotal) +
                        "&ImporteTotal=" + formatearImporte(importeTotal) +
                        "&Huella=" + huellaAnteriorSafe +
                        "&FechaHoraHusoGenRegistro=" + fechaHoraHusoGenRegistro;

        System.out.println(">>> Cadena para hash: " + cadenaAFirmar);

        return calcularSHA256(cadenaAFirmar);
    }

    /**
     * Formatea un importe igual que aparece en el XML:
     * - Sin ceros de relleno innecesarios a la derecha si son .00
     * - Pero respetando los decimales que tenga el valor real.
     * La AEAT en su ejemplo muestra: CuotaTotal=0.26, ImporteTotal=1.50
     * Es decir, siempre 2 decimales con punto.
     */
    private static String formatearImporte(BigDecimal importe) {
        // Siempre 2 decimales, punto como separador (Locale.US implícito en BigDecimal.toPlainString)
        return importe.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private static String calcularSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Error en Hash SHA-256", e);
        }
    }
}