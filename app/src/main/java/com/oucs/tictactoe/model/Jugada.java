package com.oucs.tictactoe.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Jugada {
    public static final String JUGADOR_UNO="jugador1Id";
    public static final String JUGADOR_DOS="jugador2Id";
    private String jugador1Id;
    private String jugador2Id;
    private List<Integer> celdas;
    private boolean turno1;
    private Date creado;
    private String ganador;
    private String abandonoId;
    public Jugada(){
        //Este constructor es necesario para deserializar los datos de FireStore
    }

    public Jugada(String jugador1Id) {
        this.jugador1Id = jugador1Id;
        this.jugador2Id = "";
        this.celdas = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            this.celdas.add(0);
        }
        this.turno1 = true;
        this.creado = new Date();
        this.ganador = "";
        this.abandonoId = "";
    }

    public String getJugador1Id() {
        return jugador1Id;
    }

    public void setJugador1Id(String jugador1Id) {
        this.jugador1Id = jugador1Id;
    }

    public String getJugador2Id() {
        return jugador2Id;
    }

    public void setJugador2Id(String jugador2Id) {
        this.jugador2Id = jugador2Id;
    }

    public List<Integer> getCeldas() {
        return celdas;
    }

    public void setCeldas(List<Integer> celdas) {
        this.celdas = celdas;
    }

    public boolean isTurno1() {
        return turno1;
    }

    public void setTurno1(boolean turno1) {
        this.turno1 = turno1;
    }

    public Date getCreado() {
        return creado;
    }

    public void setCreado(Date creado) {
        this.creado = creado;
    }

    public String getGanador() {
        return ganador;
    }

    public void setGanador(String ganador) {
        this.ganador = ganador;
    }

    public String getAbandonoId() {
        return abandonoId;
    }

    public void setAbandonoId(String abandonoId) {
        this.abandonoId = abandonoId;
    }
}
