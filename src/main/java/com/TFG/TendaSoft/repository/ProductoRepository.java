package com.TFG.TendaSoft.repository;

import com.TFG.TendaSoft.model.Categoria;
import com.TFG.TendaSoft.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {
    List<Producto> findByCategoriaId(Integer idCategoria);
    List<Producto> findByCategoria(Categoria categoria);
    List<Producto> findByUnidadesGreaterThan(Integer stockMinimo);
}
