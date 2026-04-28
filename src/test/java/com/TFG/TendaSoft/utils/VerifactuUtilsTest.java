package com.TFG.TendaSoft.utils;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class VerifactuUtilsTest {

    // El hash SHA-256 es determinista: misma entrada siempre produce la misma salida.
    // Este test compara contra el hash real del ancla que ya está validado por la AEAT.
    @Test
    void generarHash_conDatosAnclaReal_debeProducirHashConocido() {
        String hash = VerifactuUtils.generarHashVerifactu(
                "A39200019",
                "FAC-2026-0034",
                LocalDateTime.of(2026, 4, 24, 14, 55, 28),
                "F2",
                new BigDecimal("0.26"),
                new BigDecimal("1.50"),
                "",
                "2026-04-24T14:55:28+02:00"
        );
        assertEquals("6E2E23BC818B2217DAF31014FA6044FE4C0B6B7611A71A3BEABBE234E3BA9150", hash);
    }

    @Test
    void generarHash_conDiferentesEntradas_debeProducirHashesDiferentes() {
        String hash1 = VerifactuUtils.generarHashVerifactu(
                "A39200019", "FAC-2026-0001",
                LocalDateTime.of(2026, 1, 1, 10, 0, 0),
                "F2", new BigDecimal("2.10"), new BigDecimal("12.10"),
                "", "2026-01-01T10:00:00+01:00"
        );
        String hash2 = VerifactuUtils.generarHashVerifactu(
                "A39200019", "FAC-2026-0002",
                LocalDateTime.of(2026, 1, 2, 10, 0, 0),
                "F2", new BigDecimal("2.10"), new BigDecimal("12.10"),
                hash1, "2026-01-02T10:00:00+01:00"
        );
        assertNotEquals(hash1, hash2);
    }

    @Test
    void generarHash_resultadoDebeSerHexadecimalDe64Caracteres() {
        String hash = VerifactuUtils.generarHashVerifactu(
                "B12345678", "FAC-2026-0099",
                LocalDateTime.of(2026, 3, 15, 9, 0, 0),
                "F1", new BigDecimal("21.00"), new BigDecimal("121.00"),
                "HASHPREVIO", "2026-03-15T09:00:00+01:00"
        );
        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9A-F]+"));
    }

    @Test
    void generarHash_conHashAnteriorVacio_noDebeLanzarExcepcion() {
        assertDoesNotThrow(() -> VerifactuUtils.generarHashVerifactu(
                "A39200019", "FAC-2026-0001",
                LocalDateTime.now(), "F2",
                new BigDecimal("0.26"), new BigDecimal("1.50"),
                "", "2026-01-01T10:00:00+01:00"
        ));
    }
}