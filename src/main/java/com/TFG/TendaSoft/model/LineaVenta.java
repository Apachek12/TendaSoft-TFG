package com.TFG.TendaSoft.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "linea_venta")
@Getter
@Setter
public class LineaVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_producto", length = 150, nullable = false)
    private String nombreProducto;

    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;

    @ManyToOne
    @JoinColumn(name = "Productoscodigo_barras", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "Ventaid", nullable = false)
    private Venta venta;
}