package com.TFG.TendaSoft.repository;

import com.TFG.TendaSoft.model.CierreCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CierreCajaRepository extends JpaRepository<CierreCaja, Long> {

    // Devuelve la caja activa del usuario sin fecha de cierre
    @Query("SELECT c FROM CierreCaja c WHERE c.usuario.idUsuario = :usuarioId AND c.fechaCierre IS NULL")
    Optional<CierreCaja> buscarCajaAbiertaDeUsuario(@Param("usuarioId") Integer usuarioId);
}