package net.salesianos.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerAnswers implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String player;
    private final char letter;
    private final Map<String, String> answers;
    public static final String[] CATEGORIES = {
            "Nombre",
            "Animal",
            "Color",
            "País",
            "Cosa"
    };

    public PlayerAnswers(String player, char letter) {
        this.player    = player;
        this.letter = letter;
        this.answers = new LinkedHashMap<>();
        for (String category : CATEGORIES) {
            answers.put(category, "");
        }
    }

    public void setAnswer(String category, String answer) {
        if (answers.containsKey(category)) {
            answers.put(category, answer == null ? "" : answer.trim());
        }
    }

    public String getAnswer(String category) {
        return answers.getOrDefault(category, "");
    }

    public Map<String, String> getAnswers() {
        return Collections.unmodifiableMap(answers);
    }

    public String getPlayer() {
        return player;
    }

    public char getLetter() {
        return letter;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Respuestas de ").append(player)
                .append(" (letra '").append(letter).append("'):\n");
        answers.forEach((cat, resp) ->
                sb.append("  ").append(cat).append(": ")
                        .append(resp.isEmpty() ? "(en blanco)" : resp)
                        .append('\n'));
        return sb.toString();
    }
}