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
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal total; // Total con IVA

    // --- NUEVOS CAMPOS PARA VERIFACTU ---
    @Column(name = "base_imponible_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal baseImponibleTotal; // Total sin IVA

    @Column(name = "cuota_iva_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal cuotaIvaTotal; // Solo los impuestos

    @Column(name = "tipo_factura", length = 2, nullable = false)
    private String tipoFactura; // "F1" (Completa) o "F2" (Simplificada/Ticket)

    @Column(name = "nif_cliente", length = 15)
    private String nifCliente; // Obligatorio si es F1

    @Column(name = "nombre_cliente", length = 150)
    private String nombreCliente; // Obligatorio si es F1
    // ------------------------------------

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