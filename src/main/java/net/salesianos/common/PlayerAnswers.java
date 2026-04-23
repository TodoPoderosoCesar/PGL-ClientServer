package net.salesianos.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerAnswers implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String jugador;
    private final char letra;
    private final Map<String, String> respuestas;
    public static final String[] CATEGORIAS = {
            "Nombre",
            "Animal",
            "Color",
            "País",
            "Cosa"
    };

    public PlayerAnswers(String jugador, char letra) {
        this.jugador    = jugador;
        this.letra      = letra;
        this.respuestas = new LinkedHashMap<>();
        for (String categoria : CATEGORIAS) {
            respuestas.put(categoria, "");
        }
    }

    public void setRespuesta(String categoria, String respuesta) {
        if (respuestas.containsKey(categoria)) {
            respuestas.put(categoria, respuesta == null ? "" : respuesta.trim());
        }
    }

    public String getRespuesta(String categoria) {
        return respuestas.getOrDefault(categoria, "");
    }

    public Map<String, String> getRespuestas() {
        return Collections.unmodifiableMap(respuestas);
    }

    public String getJugador() {
        return jugador;
    }

    public char getLetra() {
        return letra;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Respuestas de ").append(jugador)
                .append(" (letra '").append(letra).append("'):\n");
        respuestas.forEach((cat, resp) ->
                sb.append("  ").append(cat).append(": ")
                        .append(resp.isEmpty() ? "(en blanco)" : resp)
                        .append('\n'));
        return sb.toString();
    }
}