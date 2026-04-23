package net.salesianos.common;

import java.io.Serializable;

public class PlayerScore implements Serializable, Comparable<PlayerScore> {

    private static final long serialVersionUID = 1L;
    private final String nombre;
    private int puntuacion;

    public PlayerScore(String nombre) {
        this.nombre     = nombre;
        this.puntuacion = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void sumarPuntos(int puntos) {
        this.puntuacion += puntos;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    @Override
    public int compareTo(PlayerScore otro) {
        int cmp = Integer.compare(otro.puntuacion, this.puntuacion);
        if (cmp != 0) return cmp;
        return this.nombre.compareToIgnoreCase(otro.nombre);
    }

    @Override
    public String toString() {
        return nombre + ": " + puntuacion + " pts";
    }
}