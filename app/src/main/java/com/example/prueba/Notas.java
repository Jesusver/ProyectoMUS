package com.example.prueba;

public enum Notas {

    DO(-9, "Do"),
    DOS(-8,"Do#"),
    RE(-7,"Re"),
    RES(-6,"Re#"),
    MI(-5,"Mi"),
    FA(-4,"Fa"),
    FAS(-3,"Fa#"),
    SOL(-2,"Sol"),
    SOLS(-1,"Sol#"),
    LA(0,"La"),
    LAS(1,"La#"),
    SI(2,"Si");


    private int posicion;
    private String nombre;
    Notas(int posicion, String nombre){
        this.posicion=posicion;
        this.nombre=nombre;
    }

    public int getPosicion() {
        return posicion;
    }

    public static Notas devuelveNotaPorNombre(String nombre) {
        for (Notas n : Notas.values()){
            if (n.nombre.equals(nombre))
                return n;
        }
        return null;
    }

}
