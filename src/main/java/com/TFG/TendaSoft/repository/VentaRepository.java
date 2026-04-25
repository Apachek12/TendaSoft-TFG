package com.TFG.TendaSoft.repository;

import com.TFG.TendaSoft.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Suma el total de ventas de un usuario desde una fecha dada (para el cierre de caja)
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.usuario.idUsuario = :usuarioId AND v.fecha >= :fechaApertura")
    BigDecimal calcularTotalVentasDesde(
            @Param("usuarioId") Integer usuarioId,
            @Param("fechaApertura") LocalDateTime fechaApertura
    );

    // Igual que el anterior pero filtrado por método de pago
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.usuario.idUsuario = :usuarioId AND v.fecha >= :fechaApertura AND v.metodoPago = :metodo")
    BigDecimal calcularTotalPorMetodo(
            @Param("usuarioId") Integer usuarioId,
            @Param("fechaApertura") LocalDateTime fechaApertura,
            @Param("metodo") String metodo
    );

    // Encadenamiento VeriFactu: última factura aceptada por la AEAT
    Optional<Venta> findFirstByEstadoVerifactuOrderByIdDesc(String estadoVerifactu);

    // Numeración: última factura con un prefijo dado (ej. "FAC-2026")
    Optional<Venta> findFirstByNumeroFacturaStartingWithOrderByIdDesc(String prefijo);

    // Búsqueda exacta por número de factura (usada en el ancla del DataInitializer)
    Optional<Venta> findByNumeroFactura(String numeroFactura);

    List<Venta> findByFechaBetweenOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin);
}