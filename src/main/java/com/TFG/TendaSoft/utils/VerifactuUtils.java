package com.TFG.TendaSoft.utils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VerifactuUtils {

    /**
     * Genera la huella digital (Hash SHA-256) exacta que exige la AEAT.
     */
    public static String generarHashVerifactu(String nifEmisor, String numFactura,
                                              LocalDateTime fechaExpedicion, String tipoFactura,
                                              BigDecimal cuotaTotal, BigDecimal importeTotal,
                                              String hashAnterior) {

        // La AEAT pide un formato de fecha específico para la cadena del hash (DD-MM-YYYY)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String fechaFormateada = fechaExpedicion.format(formatter);

        // Si es la primera factura del sistema, la AEAT dice que el hash anterior no existe o va vacío
        String hashPrevioSafe = (hashAnterior == null || hashAnterior.equals("INICIO-SISTEMA")) ? "" : hashAnterior;

        // Construimos la cadena exacta según el reglamento técnico
        // Formato típico esperado: NIF + NumFactura + Fecha + Tipo + CuotaTotal + ImporteTotal + HashAnterior
        String cadenaAFirmar = String.format("%s%s%s%s%.2f%.2f%s",
                nifEmisor,
                numFactura,
                fechaFormateada,
                tipoFactura,
                cuotaTotal,
                importeTotal,
                hashPrevioSafe
        ).replace(",", "."); // Aseguramos usar punto para decimales en la cadena

        return calcularSHA256(cadenaAFirmar);
    }

    private static String calcularSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convertir a Hexadecimal en MAYÚSCULAS (Requisito habitual)
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el Hash SHA-256 para VeriFactu", e);
        }
    }
}