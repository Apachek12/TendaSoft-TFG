package com.TFG.TendaSoft.model;

import com.TFG.TendaSoft.utils.Encriptado;
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

    @Column(nullable = false)
    private String nombreEmpresa;

    @Column(length = 20, unique = true, nullable = false)
    private String cif;

    @Column(nullable = false)
    private String direccion;

    private String mensajeTicket;
    private String rutaLogo;
    private String rutaCertificado;

    @Column(nullable = false)
    private Boolean verifactuActivado;

    // Contraseña del certificado cifrada en BD
    @Convert(converter = Encriptado.class)
    @Column(name = "certificado_password", length = 500)
    private String certificadoPassword;
}