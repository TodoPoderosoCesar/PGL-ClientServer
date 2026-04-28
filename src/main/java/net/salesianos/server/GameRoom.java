package net.salesianos.server;

import net.salesianos.common.Message;
import net.salesianos.common.MessageType;
import net.salesianos.common.PlayerAnswers;
import net.salesianos.common.PlayerScore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;


public class GameRoom {

    private static final Logger LOG = Logger.getLogger(GameRoom.class.getName());
    public static final int MIN_PLAYERS = 2;
    public static final int TOTAL_ROUNDS = 3;
    private static final char[] LETTERS = "ABCDEFGHIJKLMNOPRSTUVZ".toCharArray();

    private final CopyOnWriteArrayList<ClientHandler> handlers = new CopyOnWriteArrayList<>();
    private final List<PlayerScore> score = new ArrayList<>();
    private final List<PlayerAnswers> roundAnswers = new ArrayList<>();

    private char gameLetter;
    private int gameRound = 0;
    private Phase state = Phase.WAITING;

    private int readyPlayers = 0;


    public enum Phase {
        FINISHED,
        IN_GAME,
        PROCESSING,
        WAITING_READY,
        WAITING
    }

    public synchronized void joinPlayer(ClientHandler handler, String name) {
        handlers.add(handler);
        score.add(new PlayerScore(name));

        LOG.info("Player '" + name + "' se unió. Total: " + handlers.size());

        handler.send(new Message(MessageType.JOINED_OK, "Servidor",
                "Bienvenido a la sala, " + name + "!"));

        int missingPlayers = MIN_PLAYERS - handlers.size();
        if (missingPlayers > 0) {
            broadcast(new Message(MessageType.WAITING_PLAYERS, "Servidor",
                    "Esperando " + missingPlayers + " player(es) más..."));
        } else if (state == Phase.WAITING) {
            startRound();
        }
    }

    public synchronized void removePlayer(ClientHandler handler) {
        handlers.remove(handler);
        LOG.info("Player eliminado. Quedan: " + handlers.size());

        if (state != Phase.WAITING && state != Phase.FINISHED
                && handlers.size() < MIN_PLAYERS) {
            broadcast(new Message(MessageType.INFO, "Servidor",
                    "Un player se ha desconectado. Fin de la partida."));
            state = Phase.FINISHED;
        }
    }

    private void startRound() {
        gameRound++;
        gameLetter = randomLetter();
        roundAnswers.clear();
        state = Phase.IN_GAME;

        LOG.info("Iniciando ronda " + gameRound + " con letra '" + gameLetter + "'");

        broadcast(new Message(MessageType.START_ROUND, "Servidor",
                String.valueOf(gameLetter)));
    }


    public synchronized void handleStop(String playerName) {
        if (state != Phase.IN_GAME) {
            return;
        }
        state = Phase.PROCESSING;
        LOG.info("STOP cantado por " + playerName);

        broadcast(new Message(MessageType.STOP_CALLED, playerName, playerName));
    }


    public synchronized void getAnswers(PlayerAnswers answers) {
        roundAnswers.add(answers);
        LOG.info("Respuestas recibidas de " + answers.getPlayer()
                + " (" + roundAnswers.size() + "/" + handlers.size() + ")");

        if (roundAnswers.size() >= handlers.size()) {
            evaluateRound();
        }
    }

    private void evaluateRound() {
        LOG.info("Evaluando ronda " + gameRound);

        for (PlayerAnswers pa : roundAnswers) {
            int score = GameValidator.calculateTotalScore(pa, roundAnswers);
            searchScore(pa.getPlayer()).sumScore(score);
        }

        List<PlayerScore> classification = getOrderedScore();
        broadcast(new Message(MessageType.ROUND_RESULT, "Servidor", classification));

        if (gameRound >= TOTAL_ROUNDS) {
            endGame();
        } else {
            state = Phase.WAITING_READY; // 🔥 CLAVE
            readyPlayers = 0;
            LOG.info("Esperando confirmación de jugadores...");
        }
    }

    private void endGame() {
        state = Phase.FINISHED;
        LOG.info("Partida terminada.");
        broadcast(new Message(MessageType.GAME_OVER, "Servidor", getOrderedScore()));
    }

    public void broadcast(Message message) {
        for (ClientHandler h : handlers) {
            h.send(message);
        }
    }

    public List<PlayerScore> getOrderedScore() {
        List<PlayerScore> copy = new ArrayList<>(score);
        Collections.sort(copy);
        return copy;
    }

    public char getGameLetter() {
        return gameLetter;
    }

    public Phase getState() {
        return state;
    }

    private PlayerScore searchScore(String name) {
        return score.stream()
                .filter(ps -> ps.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró marcador para: " + name));
    }

    public synchronized void playerReady() {
        if (state != Phase.WAITING_READY) return;

        readyPlayers++;
        LOG.info("Jugador listo (" + readyPlayers + "/" + handlers.size() + ")");

        if (readyPlayers >= handlers.size()) {
            LOG.info("Todos listos. Siguiente ronda.");
            startRound();
        }
    }

    private char randomLetter() {
        return LETTERS[(int) (Math.random() * LETTERS.length)];
    }
}
