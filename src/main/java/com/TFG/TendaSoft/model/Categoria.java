package com.TFG.TendaSoft.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categorias")
@Getter
@Setter
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, unique = true, nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Integer orden;

    @ManyToOne
    @JoinColumn(name = "Categoriasid")
    private Categoria categoriaPadre;
}