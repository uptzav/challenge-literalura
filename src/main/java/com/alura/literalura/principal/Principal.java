package com.alura.literalura.principal;

import com.alura.literalura.model.*;
import com.alura.literalura.service.AutorService;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvertirDatos;
import com.alura.literalura.service.LibroService;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;

public class Principal {

    // URL base para realizar consultas a la API externa
    public static final String URL_BASE = "https://gutendex.com/books/";

    // Dependencias necesarias para la funcionalidad del programa
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final ConvertirDatos conversor = new ConvertirDatos();
    private final Scanner teclado = new Scanner(System.in);
    private final LibroService libroServicio;
    private final AutorService autorServicio;

    // Constructor que inyecta las dependencias requeridas
    public Principal(LibroService libroService, AutorService autorService) {
        this.libroServicio = libroService;
        this.autorServicio = autorService;
    }

    // Método principal para mostrar el menú de opciones
    public void muestraMenu() {
        int opcion = -1;
        while (opcion != 0) {
            try {
                System.out.println("""
                --------------
                **Catálogo de Opciones*
                1.- Buscar libro por título
                2.- Listar libros registrados
                3.- Listar autores registrados
                4.- Listar autores vivos en un determinado año
                5.- Listar libros por idioma
                6.- Estadísticas de libros por número de descargas
                7.- Top 10 libros más descargados
                8.- Buscar autor por nombre
                0.- Salir
                --------------
                
                Elija la opción a través de su número:
                """);

                opcion = teclado.nextInt();
                teclado.nextLine(); // Limpiar buffer

                switch (opcion) {
                    case 1 -> buscarLibroPorTitulo();
                    case 2 -> listarLibrosRegistrados();
                    case 3 -> listarAutoresRegistrados();
                    case 4 -> buscarAutoresVivosPorAnio();
                    case 5 -> listarLibrosPorIdioma();
                    case 6 -> estadisticasLibrosPorNumDescargas();
                    case 7 -> top10LibrosMasDescargados();
                    case 8 -> buscarAutorPorNombre();
                    case 0 -> System.out.println("Cerrando la aplicación...");
                    default -> System.out.println("Opción inválida. Favor de introducir un número del menú.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número válido del menú.");
                teclado.nextLine(); // Limpiar buffer
            }
        }
    }

    // Consulta información de libros desde la API externa
    private DatosResultados obtenerDatosResultados(String tituloLibro) {
        String json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "%20"));
        return conversor.obtenerDatos(json, DatosResultados.class);
    }

    // Busca información de un libro por título
    private void buscarLibroPorTitulo() {
        System.out.print("Ingrese el título del libro que desea buscar: ");
        String tituloLibro = teclado.nextLine().toUpperCase();

        Optional<Libro> libroRegistrado = libroServicio.buscarLibroPorTitulo(tituloLibro);

        if (libroRegistrado.isPresent()) {
            System.out.println("El libro ya está registrado: " + libroRegistrado.get());
        } else {
            DatosResultados datos = obtenerDatosResultados(tituloLibro);

            if (datos.listaLibros().isEmpty()) {
                System.out.println("No se encontró el libro buscado en la API.");
            } else {
                DatosLibros datosLibros = datos.listaLibros().get(0);
                DatosAutor datosAutores = datosLibros.autores().get(0);
                Idiomas idioma = Idiomas.fromString(datosLibros.idiomas().get(0));

                Libro libro = new Libro(datosLibros);
                libro.setIdiomas(idioma);

                Optional<Autor> autorRegistrado = autorServicio.buscarAutorRegistrado(datosAutores.nombre());

                if (autorRegistrado.isPresent()) {
                    libro.setAutor(autorRegistrado.get());
                } else {
                    Autor autor = autorServicio.guardarAutor(new Autor(datosAutores));
                    libro.setAutor(autor);
                }

                try {
                    libroServicio.guardarLibro(libro);
                    System.out.println("Libro guardado exitosamente: \n" + libro);
                } catch (DataIntegrityViolationException e) {
                    System.out.println("El libro ya está registrado.");
                }
            }
        }
    }

    // Lista todos los libros registrados
    private void listarLibrosRegistrados() {
        List<Libro> libros = libroServicio.listarLibrosRegistrados();

        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados.");
        } else {
            libros.stream()
                    .sorted(Comparator.comparing(Libro::getTitulo))
                    .forEach(System.out::println);
        }
    }

    // Lista todos los autores registrados junto con sus libros
    private void listarAutoresRegistrados() {
        List<Autor> autores = autorServicio.listarAutoresRegistrados();

        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados.");
        } else {
            autores.forEach(autor -> {
                List<Libro> librosPorAutor = libroServicio.buscarLibrosPorAutorId(autor.getId());
                System.out.println("Autor: " + autor.getNombre());
                System.out.println("Libros: " + (librosPorAutor.isEmpty() ? "No tiene libros registrados" : librosPorAutor));
            });
        }
    }

    // Busca autores que estaban vivos en un año específico
    private void buscarAutoresVivosPorAnio() {
        System.out.print("Ingrese el año para buscar autores vivos: ");
        int anio = teclado.nextInt();

        List<Autor> autores = autorServicio.buscarAutoresVivosPorAnio(anio);

        if (autores.isEmpty()) {
            System.out.println("No se encontraron autores vivos para ese año.");
        } else {
            autores.forEach(System.out::println);
        }
    }

    // Lista libros por idioma
    private void listarLibrosPorIdioma() {
        System.out.println("Idiomas disponibles:");
        System.out.println("es - Español");
        System.out.println("en - Inglés");
        System.out.println("fr - Francés");
        System.out.println("pt - Portugués");
        System.out.println("hu - Húngaro");
        System.out.println("fi - Finés");
        System.out.print("Ingrese el idioma deseado (es, en, fr, pt, hu, fi): ");

        String idioma = teclado.nextLine();

        try {
            List<Libro> libros = libroServicio.buscarLibroPorIdiomas(Idiomas.fromString(idioma));

            if (libros.isEmpty()) {
                System.out.println("No hay libros registrados para el idioma: " + obtenerNombreIdioma(idioma));
            } else {
                System.out.println("Libros disponibles en " + obtenerNombreIdioma(idioma) + ":");
                libros.forEach(System.out::println);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Idioma inválido. Por favor, seleccione uno de los idiomas disponibles.");
        }
    }

    // Método auxiliar para obtener el nombre completo del idioma
    private String obtenerNombreIdioma(String codigo) {
        return switch (codigo.toLowerCase()) {
            case "es" -> "Español";
            case "en" -> "Inglés";
            case "fr" -> "Francés";
            case "pt" -> "Portugués";
            case "hu" -> "Húngaro";
            case "fi" -> "Finés";
            default -> "Desconocido";
        };
    }

    // Muestra estadísticas de descargas de libros
    private void estadisticasLibrosPorNumDescargas() {
        List<Libro> libros = libroServicio.listarLibrosRegistrados();

        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados.");
        } else {
            DoubleSummaryStatistics stats = libros.stream()
                    .mapToDouble(Libro::getNumeroDescargas)
                    .summaryStatistics();

            System.out.printf("Media: %.2f, Máximo: %.0f, Mínimo: %.0f\n",
                    stats.getAverage(), stats.getMax(), stats.getMin());
        }
    }

    // Muestra el top 10 de libros más descargados
    private void top10LibrosMasDescargados() {
        List<Libro> top10 = libroServicio.listarTop10LibrosMasDescargados();

        if (top10.isEmpty()) {
            System.out.println("No hay libros suficientes para mostrar el Top 10.");
        } else {
            top10.forEach(System.out::println);
        }
    }

    // Busca autores por nombre
    private void buscarAutorPorNombre() {
        System.out.print("Ingrese el nombre del autor: ");
        String nombre = teclado.nextLine().toUpperCase();

        List<Autor> autores = autorServicio.buscarAutorPorNombre(nombre);

        if (autores.isEmpty()) {
            System.out.println("No se encontraron autores con ese nombre.");
        } else {
            autores.forEach(System.out::println);
        }
    }
}
