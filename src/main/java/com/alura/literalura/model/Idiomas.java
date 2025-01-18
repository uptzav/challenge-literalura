package com.alura.literalura.model;

public enum Idiomas {
    //inglés
    en("en"),
    //español
    es("es"),
    //francés
    fr("fr"),
    //húngaro
    hu("hu"),
    //finés
    fi("fi"),
    //portugués
    pt("pt");

    private String idiomasapi;

    Idiomas (String idiomasGutendex) {
        this.idiomasapi = idiomasGutendex;
    }

    //Metodo para convertir el idioma de tipo Idioma a tipo cadena
    public static Idiomas fromString (String text) {
        for (Idiomas idiomas : Idiomas.values()) {
            if (idiomas.idiomasapi.equalsIgnoreCase(text)) {
                return idiomas;
            }
        }
        throw new IllegalArgumentException("No se encontró ningún idioma: " + text);
    }
}
