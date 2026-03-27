package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.dto.EstadisticasDTO;
import com.TFG.TendaSoft.model.Venta;
import com.TFG.TendaSoft.repository.VentaRepository;
import com.TFG.TendaSoft.repository.LineaVentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EstadisticasService {

    private final VentaRepository ventaRepository;
    private final LineaVentaRepository lineaVentaRepository;

    public EstadisticasDTO obtenerResumenFinanciero() {
        List<Venta> todas = ventaRepository.findAll();

        BigDecimal total = todas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long numTickets = (long) todas.size();

        BigDecimal ticketMedio = numTickets > 0
                ? total.divide(BigDecimal.valueOf(numTickets), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Agrupar por método de pago
        Map<String, BigDecimal> porMetodo = new HashMap<>();
        todas.forEach(v -> {
            porMetodo.merge(v.getMetodoPago(), v.getTotal(), BigDecimal::add);
        });

        return EstadisticasDTO.builder()
                .totalFacturado(total)
                .totalTickets(numTickets)
                .ticketMedio(ticketMedio)
                .ventasPorMetodoPago(porMetodo)
                .productosVendidos(lineaVentaRepository.count()) // Simplificado: número de líneas
                .build();
    }
}