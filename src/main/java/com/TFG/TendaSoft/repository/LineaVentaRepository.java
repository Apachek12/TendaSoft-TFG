package com.TFG.TendaSoft.repository;

import com.TFG.TendaSoft.model.LineaVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LineaVentaRepository extends JpaRepository<LineaVenta, Long> {
}
