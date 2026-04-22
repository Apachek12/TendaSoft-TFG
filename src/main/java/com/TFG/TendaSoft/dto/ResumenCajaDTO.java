package com.TFG.TendaSoft.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumenCajaDTO {
    private BigDecimal totalVentas = BigDecimal.ZERO;
    private BigDecimal efectivo = BigDecimal.ZERO;
    private BigDecimal tarjeta = BigDecimal.ZERO;
    private BigDecimal otros = BigDecimal.ZERO;
}