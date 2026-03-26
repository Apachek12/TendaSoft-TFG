package com.TFG.TendaSoft.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "datos_negocio")
@Getter
@Setter
public class DatosNegocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_empresa", length = 100, nullable = false)
    private String nombreEmpresa;

    @Column(length = 20, unique = true, nullable = false)
    private String cif;

    @Column(length = 255, nullable = false)
    private String direccion;

    @Column(name = "mensaje_ticket", length = 255)
    private String mensajeTicket;

    @Column(name = "ruta_logo", length = 255)
    private String rutaLogo;

    // tinyint en BD se suele mapear como Boolean en Java
    @Column(name = "verifactu_activado", nullable = false)
    private Boolean verifactuActivado;

    @Column(name = "ruta_certificado", length = 255)
    private String rutaCertificado;
}