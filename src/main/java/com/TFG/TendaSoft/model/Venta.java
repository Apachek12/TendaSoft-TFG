package com.TFG.TendaSoft.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "venta")
@Getter
@Setter
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Long para mapear bigint(19)

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name = "metodo_pago", length = 20, nullable = false)
    private String metodoPago;

    @Column(name = "numero_factura", length = 50, unique = true, nullable = false)
    private String numeroFactura;

    @Column(name = "hash_verifactu", length = 64, nullable = false)
    private String hashVerifactu;

    @Column(name = "hash_anterior", length = 64)
    private String hashAnterior;

    @Column(name = "estado_verifactu", length = 20, nullable = false)
    private String estadoVerifactu;

    @ManyToOne
    @JoinColumn(name = "Usuariosid_usuario", nullable = false)
    private Usuario usuario;
}