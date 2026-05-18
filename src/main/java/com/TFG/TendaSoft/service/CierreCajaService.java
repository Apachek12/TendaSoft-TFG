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

@Service
@RequiredArgsConstructor
public class CierreCajaService {

    private final CierreCajaRepository cierreCajaRepository;
    private final VentaRepository ventaRepository;

    public CierreCaja obtenerCajaAbierta(Integer idUsuario) {
        return cierreCajaRepository.buscarCajaAbiertaDeUsuario(idUsuario).orElse(null);
    }

    public ResumenCajaDTO obtenerResumenActual(Integer idUsuario) {
        CierreCaja caja = obtenerCajaAbierta(idUsuario);
        if (caja == null) return new ResumenCajaDTO();

        LocalDateTime desde = caja.getFechaApertura();

        BigDecimal total    = ventaRepository.calcularTotalVentasDesde(idUsuario, desde);
        BigDecimal efectivo = ventaRepository.calcularTotalPorMetodo(idUsuario, desde, "EFECTIVO");
        BigDecimal tarjeta  = ventaRepository.calcularTotalPorMetodo(idUsuario, desde, "TARJETA");
        BigDecimal otros    = total.subtract(efectivo).subtract(tarjeta);

        return new ResumenCajaDTO(
                total,
                efectivo,
                tarjeta,
                otros.compareTo(BigDecimal.ZERO) > 0 ? otros : BigDecimal.ZERO
        );
    }

    @Transactional
    public CierreCaja abrirCaja(BigDecimal fondoInicial, Usuario usuario) {
        cierreCajaRepository.buscarCajaAbiertaDeUsuario(usuario.getIdUsuario())
                .ifPresent(c -> { throw new IllegalStateException("Ya hay una caja abierta."); });

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

        // Cálculo descuadre efectivo
        BigDecimal dineroEsperado = caja.getFondoInicial().add(resumen.getEfectivo());

        caja.setFechaCierre(LocalDateTime.now());
        caja.setTotalVentas(resumen.getTotalVentas());
        caja.setFondoFinal(dineroFisicoContado);
        caja.setDescuadre(dineroFisicoContado.subtract(dineroEsperado));

        return cierreCajaRepository.save(caja);
    }
}