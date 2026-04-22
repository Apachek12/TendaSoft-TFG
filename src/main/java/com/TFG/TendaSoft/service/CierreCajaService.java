package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.dto.ResumenCajaDTO;
import com.TFG.TendaSoft.model.CierreCaja;
import com.TFG.TendaSoft.model.Usuario;
import com.TFG.TendaSoft.repository.CierreCajaRepository;
import com.TFG.TendaSoft.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CierreCajaService {

    private final CierreCajaRepository cierreCajaRepository;
    private final VentaRepository ventaRepository;

    // Método para que el Controller y React sepan si hay algo abierto
    public CierreCaja obtenerCajaAbierta(Integer idUsuario) {
        return cierreCajaRepository.buscarCajaAbiertaDeUsuario(idUsuario).orElse(null);
    }

    // Método que usa el DTO para el resumen en vivo
    public ResumenCajaDTO obtenerResumenActual(Integer idUsuario) {
        CierreCaja caja = obtenerCajaAbierta(idUsuario);
        if (caja == null) return new ResumenCajaDTO();

        // Calculamos el total de ventas desde que se abrió esta caja
        BigDecimal total = ventaRepository.calcularTotalVentasDesde(idUsuario, caja.getFechaApertura());
        BigDecimal totalFinal = (total != null) ? total : BigDecimal.ZERO;

        // Aquí podrías desglosar efectivo/tarjeta si tu repositorio lo permite
        // Por ahora, devolvemos el total acumulado
        return new ResumenCajaDTO(totalFinal, totalFinal, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Transactional
    public CierreCaja abrirCaja(BigDecimal fondoInicial, Usuario usuario) {
        cierreCajaRepository.buscarCajaAbiertaDeUsuario(usuario.getIdUsuario())
                .ifPresent(cajaAntigua -> {
                    throw new IllegalStateException("Ya tienes una caja abierta.");
                });

        CierreCaja nuevoCierre = new CierreCaja();
        nuevoCierre.setFechaApertura(LocalDateTime.now());
        nuevoCierre.setFondoInicial(fondoInicial);
        nuevoCierre.setUsuario(usuario);
        nuevoCierre.setTotalVentas(BigDecimal.ZERO);
        nuevoCierre.setFondoFinal(BigDecimal.ZERO);
        nuevoCierre.setDescuadre(BigDecimal.ZERO);

        return cierreCajaRepository.save(nuevoCierre);
    }

    @Transactional
    public CierreCaja cerrarCaja(Integer idUsuario, BigDecimal dineroFisicoContado) {
        CierreCaja caja = obtenerCajaAbierta(idUsuario);
        if (caja == null) throw new RuntimeException("No hay caja abierta.");

        ResumenCajaDTO resumen = obtenerResumenActual(idUsuario);

        caja.setFechaCierre(LocalDateTime.now());
        caja.setTotalVentas(resumen.getTotalVentas());
        caja.setFondoFinal(dineroFisicoContado);

        BigDecimal dineroEsperado = caja.getFondoInicial().add(resumen.getTotalVentas());
        caja.setDescuadre(dineroFisicoContado.subtract(dineroEsperado));

        return cierreCajaRepository.save(caja);
    }
}