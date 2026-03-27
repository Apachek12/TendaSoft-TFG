package com.TFG.TendaSoft.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class VentaListadoDTO {
    private Long idVenta;
    private String numeroFactura;
    private LocalDateTime fecha;
    private BigDecimal total;
    private String metodoPago;
    private String estadoVerifactu; // Para el icono verde/rojo/naranja
    private String nombreCajero;    // <-- ¡AQUÍ ESTÁ LA CLAVE! Reemplaza al objeto Usuario completo
}