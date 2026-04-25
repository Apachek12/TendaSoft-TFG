package com.TFG.TendaSoft.repository;

import com.TFG.TendaSoft.model.DatosNegocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatosNegocioRepository extends JpaRepository<DatosNegocio, Integer> {

    Optional<DatosNegocio> findTopByOrderByIdAsc();
}