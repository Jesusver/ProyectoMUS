package com.example.prueba;

public enum Notas {

    DO(-9),
    DOS(-8),
    RE(-7),
    RES(-6),
    MI(-5),
    FA(-4),
    FAS(-3),
    SOL(-2),
    SOLS(-1),
    LA(0),
    LAS(1),
    SI(2);


    private int posicion;
    Notas(int posicion){
        this.posicion=posicion;
    }

    public int getPosicion() {
        return posicion;
    }
}
