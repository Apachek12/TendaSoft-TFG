package com.TFG.TendaSoft.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VentaListadoDTO {
    private Long idVenta;
    private String numeroFactura;
    private LocalDateTime fecha;
    private BigDecimal total;
    private String metodoPago;
    private String estadoVerifactu;
    private String nombreCajero;
    private List<LineaVentaDTO> lineas;
}