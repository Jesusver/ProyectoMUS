package com.example.prueba;

import java.lang.reflect.Array;
import java.util.ArrayList;

public enum Octavas {
    Primera("Primera", 1),
    Segunda("Segunda", 2),
    Tercera("Tercera", 3),
    Cuarta("Cuarta", 4),
    Quinta("Quinta", 5),
    Sexta("Sexta", 6),
    Septima("Septima", 7);

    private String nombre;
    private int numero;


    private Octavas(String nombre, int numero){
        this.nombre=nombre;
        this.numero=numero;
    }

    public static ArrayList<String> devuelveNombreOctavas() {
        ArrayList<String> retorno=new ArrayList<>();
        for (Octavas o : Octavas.values()){
            retorno.add(o.nombre);
        }
        return retorno;
    }

    public static Octavas devuelveOctavaPorNombre(String nombre) {
        for (Octavas o : Octavas.values()){
            if (o.nombre.equals(nombre))
                return o;
        }
        return null;
    }

    public int getNumero() {
        return this.numero;
    }
}
