package com.TFG.TendaSoft.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cierre_caja")
@Getter
@Setter
public class CierreCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "fondo_inicial", precision = 10, scale = 2, nullable = false)
    private BigDecimal fondoInicial;

    @Column(name = "total_ventas", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalVentas;

    @Column(name = "fondo_final", precision = 10, scale = 2, nullable = false)
    private BigDecimal fondoFinal;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal descuadre;

    @ManyToOne
    @JoinColumn(name = "Usuariosid_usuario", nullable = false)
    private Usuario usuario;
}
