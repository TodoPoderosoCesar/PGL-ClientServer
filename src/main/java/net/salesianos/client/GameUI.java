package net.salesianos.client;

import net.salesianos.common.PlayerScore;

import java.util.List;

public class GameUI {

    private static final String SEPARATOR = "═".repeat(50);
    private static final String SEPARATOR_2 = "─".repeat(50);

    private GameUI() {}

    public static void printWelcome() {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("       ¡Bienvenido a STOP Online!");
        System.out.println(SEPARATOR);
        System.out.println();
    }

    public static void printEntranceToRoom(String msg) {
        System.out.println();
        System.out.println("✔  " + msg);
        System.out.println();
    }

    public static void printWaitingForPlayers(String info) {
        System.out.println("⏳ " + info);
    }

    public static void printRoundStart(char letter) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("   🎯  NUEVA RONDA — Letra: [ " + letter + " ]");
        System.out.println(SEPARATOR);
        System.out.println();
        System.out.println("  Escribe tus respuestas. Cuando termines escribe STOP.");
        System.out.println("  (o escribe STOP en cualquier momento para parar la ronda)");
        System.out.println();
    }

    public static String promptCategory(String category, char letter) {
        return "  " + category + " (" + letter + "): ";
    }

    public static void printStop(String playerName, boolean isMine) {
        System.out.println();
        if (isMine) {
            System.out.println("🛑  ¡Has cantado STOP! Enviando tus respuestas...");
        } else {
            System.out.println("🛑  ¡" + playerName + " ha cantado STOP! Entrega tus respuestas.");
        }
        System.out.println();
    }

    public static void printForcedStop() {
        System.out.println("  ⚠  El tiempo se acaba, enviando lo que tengas...");
        System.out.println();
    }

    public static void printResults(List<PlayerScore> score, boolean isLast) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println(isLast ? "   🏆  CLASIFICACIÓN FINAL" : "   📊  RESULTADOS DE LA RONDA");
        System.out.println(SEPARATOR);

        int position = 1;
        for (PlayerScore ps : score) {
            String trophy = switch (position) {
                case 1 -> "🥇";
                case 2 -> "🥈";
                case 3 -> "🥉";
                default -> "   ";
            };
            System.out.printf("  %s  %-20s %4d pts%n",
                    trophy, ps.getName(), ps.getScore());
            position++;
        }

        System.out.println(SEPARATOR_2);
        System.out.println();
    }

    public static void printGameFinished(List<PlayerScore> score) {
        printResults(score, true);
        if (!score.isEmpty()) {
            System.out.println("  🎉  ¡" + score.get(0).getName() + " gana la partida!");
        }
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  Gracias por jugar. ¡Hasta la próxima!");
        System.out.println(SEPARATOR);
        System.out.println();
    }

    public static void printInfo(String info) {
        System.out.println("ℹ  " + info);
    }

    public static void printErrorMessage(String error) {
        System.err.println("❌  Error: " + error);
    }

    public static void printDisconnection() {
        System.out.println();
        System.out.println("⚠  Se ha perdido la conexión con el servidor.");
        System.out.println();
    }
}