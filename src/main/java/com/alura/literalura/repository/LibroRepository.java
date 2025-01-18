package com.alura.literalura.repository;

import com.alura.literalura.model.Idiomas;
import com.alura.literalura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    //Busca el libro por el nombre
    Optional<Libro> findByTituloIgnoreCase(String titulo);

    //Busca los libros por el id del autor
    @Query("SELECT l FROM Libro l WHERE l.autor.id = :autorId")
    List<Libro> buscarLibrosPorAutorId(Long autorId);

    //Busca los libros por idioma
    List<Libro> findByIdiomas(Idiomas nombreIdioma);

    //Busca los libros y muestra los 10 libros más descargados
    @Query(value = "SELECT * FROM libros ORDER BY numero_descargas DESC LIMIT 10;", nativeQuery = true)
    List<Libro> top10LibrosMasDescargados();
}