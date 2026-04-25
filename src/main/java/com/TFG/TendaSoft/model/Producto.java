package com.TFG.TendaSoft.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Getter
@Setter
public class Producto {

    @Id
    @Column(name = "codigo_barras", length = 50, unique = true, nullable = false)
    private String codigoBarras;

    @Column(length = 100, unique = true, nullable = false)
    private String nombre;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer unidades;

    @Column(name = "porcentaje_iva", precision = 5, scale = 2, nullable = false)
    private BigDecimal porcentajeIva;

    @Column(name = "url_imagen", length = 255)
    private String urlImagen;

    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Categoriasid", nullable = false)
    @JsonBackReference
    private Categoria categoria;
}