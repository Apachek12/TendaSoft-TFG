package com.TFG.TendaSoft.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class EstadisticasDTO {
    private BigDecimal totalFacturado;
    private Long totalTickets;
    private BigDecimal ticketMedio;
    private Map<String, BigDecimal> ventasPorMetodoPago;
    private Long productosVendidos;
    private Map<String, Long> productosMasVendidos;
}