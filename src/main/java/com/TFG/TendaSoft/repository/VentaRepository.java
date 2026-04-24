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
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.usuario.idUsuario = :usuarioId AND v.fecha >= :fechaApertura")
    BigDecimal calcularTotalVentasDesde(
            @Param("usuarioId") Integer usuarioId,
            @Param("fechaApertura") LocalDateTime fechaApertura
    );

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.usuario.idUsuario = :usuarioId AND v.fecha >= :fechaApertura AND v.metodoPago = :metodo")
    BigDecimal calcularTotalPorMetodo(
            @Param("usuarioId") Integer usuarioId,
            @Param("fechaApertura") LocalDateTime fechaApertura,
            @Param("metodo") String metodo
    );

    Optional<Venta> findFirstByEstadoVerifactuOrderByIdDesc(String estadoVerifactu);

    Optional<Venta> findFirstByOrderByIdDesc();

    Optional<Venta> findFirstByNumeroFacturaStartingWithOrderByIdDesc(String prefijo);

    List<Venta> findByFechaBetweenOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin);

}
