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

        // 1. EL CANDADO: Comprobamos si este usuario ya tiene una caja sin cerrar
        cierreCajaRepository.buscarCajaAbiertaDeUsuario(usuario.getIdUsuario())
                .ifPresent(cajaAntigua -> {
                    throw new IllegalStateException(
                            "¡Cuidado! Tienes una caja abierta desde el " +
                                    cajaAntigua.getFechaApertura().toLocalDate() +
                                    ". Debes cerrarla antes de abrir una nueva."
                    );
                });

        // 2. Si no saltó el error anterior, creamos la caja nueva con normalidad
        CierreCaja nuevoCierre = new CierreCaja();
        nuevoCierre.setFechaApertura(LocalDateTime.now());
        nuevoCierre.setFondoInicial(fondoInicial);
        nuevoCierre.setUsuario(usuario);

        nuevoCierre.setTotalVentas(BigDecimal.ZERO);
        nuevoCierre.setFondoFinal(BigDecimal.ZERO);
        nuevoCierre.setDescuadre(BigDecimal.ZERO);
        // Dejamos la fecha de cierre en null explícitamente (así sabemos que está abierta)
        nuevoCierre.setFechaCierre(null);

        return cierreCajaRepository.save(nuevoCierre);
    }

    public CierreCaja cerrarCaja(Integer idUsuario, BigDecimal dineroFisicoContado) {

        // ¡Buscamos su caja abierta mágicamente!
        CierreCaja caja = cierreCajaRepository.buscarCajaAbiertaDeUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Este usuario no tiene ninguna caja abierta actualmente."));

        caja.setFechaCierre(LocalDateTime.now());

        // Calculamos las ventas
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