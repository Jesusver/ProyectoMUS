package com.example.prueba;

public class GeneraNota {

    private final int FRECUENCIA_LA = 110;
    private static final GeneraNota ourInstance = new GeneraNota();
    private final double CONST = Math.pow(2,1/12);


    public static GeneraNota getInstance(){
        return ourInstance;
    }

    public double devuelveFrecuenciaNota(Notas n, int octava){
        return Math.pow(CONST,n.getPosicion())*FRECUENCIA_LA*Math.pow(2,octava-1);
    }
}
