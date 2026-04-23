package com.TFG.TendaSoft.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "Categoriasid")
    private Categoria categoriaPadre;


    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    @JsonManagedReference
    private java.util.List<Producto> productos;
}