package com.TFG.TendaSoft.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LineaVentaDTO {
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}