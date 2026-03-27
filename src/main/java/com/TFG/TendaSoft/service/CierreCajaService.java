package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.model.CierreCaja;
import com.TFG.TendaSoft.model.Usuario;
import com.TFG.TendaSoft.repository.CierreCajaRepository;
import com.TFG.TendaSoft.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CierreCajaService {

    private final CierreCajaRepository cierreCajaRepository;
    private final VentaRepository ventaRepository;

    public CierreCaja abrirCaja(BigDecimal fondoInicial, Usuario usuario) {

        cierreCajaRepository.buscarCajaAbiertaDeUsuario(usuario.getIdUsuario())
                .ifPresent(cajaAntigua -> {
                    throw new IllegalStateException(
                            "¡Cuidado! Tienes una caja abierta desde el " +
                                    cajaAntigua.getFechaApertura().toLocalDate() +
                                    ". Debes cerrarla antes de abrir una nueva."
                    );
                });

        CierreCaja nuevoCierre = new CierreCaja();
        nuevoCierre.setFechaApertura(LocalDateTime.now());
        nuevoCierre.setFondoInicial(fondoInicial);
        nuevoCierre.setUsuario(usuario);

        nuevoCierre.setTotalVentas(BigDecimal.ZERO);
        nuevoCierre.setFondoFinal(BigDecimal.ZERO);
        nuevoCierre.setDescuadre(BigDecimal.ZERO);
        nuevoCierre.setFechaCierre(null);

        return cierreCajaRepository.save(nuevoCierre);
    }

    public CierreCaja cerrarCaja(Integer idUsuario, BigDecimal dineroFisicoContado) {

        CierreCaja caja = cierreCajaRepository.buscarCajaAbiertaDeUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Este usuario no tiene ninguna caja abierta actualmente."));

        caja.setFechaCierre(LocalDateTime.now());

        BigDecimal totalVentasCalculado = ventaRepository.calcularTotalVentasDesde(
                caja.getUsuario().getIdUsuario(),
                caja.getFechaApertura()
        );

        if (totalVentasCalculado == null) {
            totalVentasCalculado = BigDecimal.ZERO;
        }

        caja.setTotalVentas(totalVentasCalculado);
        caja.setFondoFinal(dineroFisicoContado);

        BigDecimal dineroEsperado = caja.getFondoInicial().add(totalVentasCalculado);
        BigDecimal descuadre = dineroFisicoContado.subtract(dineroEsperado);

        caja.setDescuadre(descuadre);

        return cierreCajaRepository.save(caja);
    }
}