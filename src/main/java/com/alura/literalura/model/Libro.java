package com.alura.literalura.model;


import jakarta.persistence.*;

import java.util.OptionalDouble;

@Entity
@Table(name = "libros")
public class Libro {

    //Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String titulo;
    @Enumerated(EnumType.STRING)
    private Idiomas idiomas;
    private Double numeroDescargas;
    @ManyToOne
    private Autor autor;

    //Constructor con atributos
    public Libro() {}

    //Constructor con atributos
    public Libro(DatosLibros datosLibros) {
        this.titulo = datosLibros.titulo();
        this.autor = new Autor(datosLibros.autores().get(0));
        this.idiomas = Idiomas.fromString(datosLibros.idiomas().get(0));
        this.numeroDescargas = OptionalDouble.of(datosLibros.numeroDescargas()).orElse(0);
    }

    //Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Autor getAutor() {
        return autor;
    }

    public void setAutor(Autor autor) {
        this.autor = autor;
    }

    public Idiomas getIdiomas() {
        return idiomas;
    }

    public void setIdiomas(Idiomas idiomas) {
        this.idiomas = idiomas;
    }

    public Double getNumeroDescargas() {
        return numeroDescargas;
    }

    public void setNumeroDescargas(Double numeroDescargas) {
        this.numeroDescargas = numeroDescargas;
    }

    //toString
    @Override
    public String toString() {
        return String.format(
                "----- LIBRO -----\nTítulo: %s\nAutores: %s\nIdiomas: %s\nNúmero de Descargas: %.2f\n-----------------\n",
                titulo, autor.getNombre(), idiomas, numeroDescargas
        );
    }

}