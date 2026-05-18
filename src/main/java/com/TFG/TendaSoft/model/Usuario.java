package com.TFG.TendaSoft.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre_real", length = 100, nullable = false)
    private String nombreReal;

    @Column(name = "nombre_usuario", length = 50, unique = true, nullable = false)
    private String nombreUsuario;

    @Column(name = "hash_contrasena", length = 255, nullable = false)
    private String hashContrasena;

    @Column(length = 20, nullable = false)
    private String rol;

    private Boolean activo = true;
}