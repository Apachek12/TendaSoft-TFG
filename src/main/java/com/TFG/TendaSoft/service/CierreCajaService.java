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

        LocalDateTime desde = caja.getFechaApertura();

        // 1. Calculamos el total global
        BigDecimal totalGlobal = ventaRepository.calcularTotalVentasDesde(idUsuario, desde);

        // 2. Calculamos el desglose (importante que los Strings coincidan con lo que envías desde React)
        BigDecimal efectivo = ventaRepository.calcularTotalPorMetodo(idUsuario, desde, "EFECTIVO");
        BigDecimal tarjeta = ventaRepository.calcularTotalPorMetodo(idUsuario, desde, "TARJETA");

        // 3. Calculamos "otros" (si hubiera) o simplemente lo dejamos a cero
        BigDecimal otros = totalGlobal.subtract(efectivo).subtract(tarjeta);

        // Ahora pasamos cada valor a su sitio correcto en el DTO
        // Estructura: (Total, Efectivo, Tarjeta, Otros)
        return new ResumenCajaDTO(
                totalGlobal != null ? totalGlobal : BigDecimal.ZERO,
                efectivo != null ? efectivo : BigDecimal.ZERO,
                tarjeta != null ? tarjeta : BigDecimal.ZERO,
                otros.compareTo(BigDecimal.ZERO) > 0 ? otros : BigDecimal.ZERO
        );
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

        // 1. Obtenemos el resumen desglosado que acabamos de arreglar
        ResumenCajaDTO resumen = obtenerResumenActual(idUsuario);

        caja.setFechaCierre(LocalDateTime.now());
        caja.setTotalVentas(resumen.getTotalVentas());
        caja.setFondoFinal(dineroFisicoContado);

        // 2. CÁLCULO DEL DINERO QUE DEBERÍA HABER (Solo efectivo)
        BigDecimal dineroEsperado = caja.getFondoInicial().add(resumen.getEfectivo());

        // 3. CÁLCULO DEL DESCUADRE
        caja.setDescuadre(dineroFisicoContado.subtract(dineroEsperado));

        return cierreCajaRepository.save(caja);
    }
}