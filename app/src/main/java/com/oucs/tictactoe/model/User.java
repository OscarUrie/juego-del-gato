package com.oucs.tictactoe.model;

public class User {
    public static final String NOMBRE="nombre";
    public static final String PUNTOS="puntos";
    public static final String PARTIDAS="partidas";
    String nombre;
    int puntos;
    int partidas;

    public User() {
    }

    public User(String nombre, int puntos, int partidas) {
        this.nombre = nombre;
        this.puntos = puntos;
        this.partidas = partidas;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public int getPartidas() {
        return partidas;
    }

    public void setPartidas(int partidas) {
        this.partidas = partidas;
    }
}
