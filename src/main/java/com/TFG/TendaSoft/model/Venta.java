package com.TFG.TendaSoft.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "numero_factura", length = 50, unique = true, nullable = false)
    private String numeroFactura;

    @Column(name = "tipo_factura", length = 2, nullable = false)
    private String tipoFactura;

    @Column(name = "metodo_pago", length = 20, nullable = false)
    private String metodoPago;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name = "base_imponible_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal baseImponibleTotal;

    @Column(name = "cuota_iva_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal cuotaIvaTotal;

    @Column(name = "nif_cliente", length = 15)
    private String nifCliente;

    @Column(name = "nombre_cliente", length = 150)
    private String nombreCliente;

    // ── VeriFactu ────────────────────────────────────────────────────────────

    @Column(name = "estado_verifactu", length = 50, nullable = false)
    private String estadoVerifactu;

    @Column(name = "hash_verifactu", length = 64, nullable = false)
    private String hashVerifactu;

    @Column(name = "hash_anterior", length = 64)
    private String hashAnterior;

    @Column(name = "numero_factura_anterior", length = 50)
    private String numeroFacturaAnterior;

    @Column(name = "fecha_anterior")
    private LocalDateTime fechaAnterior;

    @Column(name = "cif_emisor_anterior", length = 15)
    private String cifEmisorAnterior;

    // XML firmado para reenvíos sin regenerar el hash
    @Column(name = "xml_firmado", columnDefinition = "TEXT")
    private String xmlFirmado;

    // ── Relaciones ───────────────────────────────────────────────────────────

    @ManyToOne
    @JoinColumn(name = "Usuariosid_usuario", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LineaVenta> lineas;
}