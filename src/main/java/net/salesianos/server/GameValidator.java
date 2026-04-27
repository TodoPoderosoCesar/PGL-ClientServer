package net.salesianos.server;

import net.salesianos.common.PlayerAnswers;
import java.util.List;
import java.util.Map;

public class GameValidator {

    private GameValidator() {}

    public static int calculateScore(String answer, char letter,
                                     List<PlayerAnswers> allAnswers, String category) {
        if (!isValid(answer, letter)) {
            return 0;
        }

        long coincidences = allAnswers.stream()
                .map(pa -> pa.getAnswer(category).trim().toLowerCase())
                .filter(r -> r.equals(answer.trim().toLowerCase()))
                .count();

        return coincidences == 1 ? 10 : 5;
    }

    public static boolean isValid(String answer, char letter) {
        if (answer == null || answer.isBlank()) {
            return false;
        }
        char first = Character.toLowerCase(answer.trim().charAt(0));
        return first == Character.toLowerCase(letter);
    }

    public static int calculateTotalScore(PlayerAnswers player,
                                          List<PlayerAnswers> allAnswers) {
        int total = 0;
        char letter = player.getLetter();

        for (Map.Entry<String, String> e : player.getAnswers().entrySet()) {
            String category = e.getKey();
            String respuesta = e.getValue();
            total += calculateScore(respuesta, letter, allAnswers, category);
        }
        return total;
    }
}
