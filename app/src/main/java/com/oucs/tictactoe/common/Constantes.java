package com.oucs.tictactoe.common;

public class Constantes {
    public static final String JUGADA_ID = "se pasa el id del jugador";
    public static final String NOMBRE_COLECCION_JUGADAS ="jugadas";
    public static final String NOMBRE_COLECCION_USUARIOS = "usuarios";
    public static final String JUEGO_EMPATADO = "hubo empate";
    public static final int[][] Gato;
    static {
        Gato=new int[][]{
                {0,1}
                ,{0,3}
                ,{0,4}
                ,{1,3}
                ,{2,2}
                ,{2,3}
                ,{3,1}
                ,{6,1}
        };
    }
}
