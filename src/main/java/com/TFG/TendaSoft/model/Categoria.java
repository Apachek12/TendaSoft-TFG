package com.TFG.TendaSoft.model;

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

    @ManyToOne
    @JoinColumn(name = "Categoriasid")
    private Categoria categoriaPadre;

    // --- ¡AÑADE ESTO! ---
    // mappedBy debe coincidir con el nombre del campo en la clase Producto
    @OneToMany(mappedBy = "categoria", fetch = FetchType.EAGER)
    @JsonManagedReference // Indica que esta parte se incluye en el JSON
    private java.util.List<Producto> productos;
}