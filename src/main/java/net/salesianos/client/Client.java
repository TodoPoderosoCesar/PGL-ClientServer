package net.salesianos.client;

import net.salesianos.common.Message;
import net.salesianos.common.MessageType;
import net.salesianos.common.PlayerAnswers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static final Logger LOG = Logger.getLogger(Client.class.getName());
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 12345;

    private volatile boolean connected = false;
    private volatile boolean stopCalled = false;
    private volatile char gameLetter;

    private Socket             socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    private String playerName;

    public static void main(String[] args) {
        String host   = args.length > 0 ? args[0] : DEFAULT_HOST;
        int    port = DEFAULT_PORT;

        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                LOG.warning("Puerto inválido '" + args[1] + "'. Usando " + DEFAULT_PORT);
            }
        }

        new Client().start(host, port);
    }

    public void start(String host, int port) {
        GameUI.printWelcome();

        try {
            connect(host, port);
            gameLoop();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "No se pudo conectar al servidor " + host + ":" + port, e);
            System.err.println("❌  No se pudo conectar al servidor. ¿Está encendido?");
        } finally {
            end();
        }
    }

    private void connect(String host, int port) throws IOException {
        socket  = new Socket(host, port);
        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());
        connected = true;

        LOG.info("Conectado a " + host + ":" + port);

        // Lanzar el hilo escucha en segundo plano
        Thread listenerThread = new Thread(new ServerListener(input, this));
        listenerThread.setDaemon(true); // muere con el hilo principal
        listenerThread.start();
    }

    private void gameLoop() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Introduce tu nombre: ");
        playerName = scanner.nextLine().trim();
        while (playerName.isBlank()) {
            System.out.print("El nombre no puede estar vacío. Introduce tu nombre: ");
            playerName = scanner.nextLine().trim();
        }
        send(new Message(MessageType.JOIN, playerName, playerName));

        while (connected) {
            waitingRound();

            if (!connected) break;

            PlayerAnswers answers = getAllAnswers(scanner);

            send(new Message(MessageType.SUBMIT_ANSWERS, playerName, answers));

            stopCalled = false;

            waitForResults();
        }
        scanner.close();
    }

    private PlayerAnswers getAllAnswers(Scanner scanner) {
        PlayerAnswers answers = new PlayerAnswers(playerName, gameLetter);
        boolean playerStop = false;

        for (String category : PlayerAnswers.CATEGORIES) {

            if (stopCalled) {
                // Otro jugador cantó STOP mientras escribíamos
                GameUI.printForcedStop();
                break;
            }

            System.out.print(GameUI.promptCategory(category, gameLetter));
            String linea = scanner.nextLine().trim();

            if (linea.equalsIgnoreCase("STOP")) {
                // Este jugador canta STOP
                playerStop = true;
                send(new Message(MessageType.STOP_CALLED, playerName, playerName));
                break;
            }

            answers.setAnswer(category, linea);
        }

        // Si cantó STOP propio, pedir las categorías que quedaron pendientes
        if (playerStop && !stopCalled) {
            answers = completeAnswersAfterStop(scanner, answers);
        }

        return answers;
    }

    private PlayerAnswers completeAnswersAfterStop(Scanner scanner, PlayerAnswers parcials) {
        System.out.println("  Completa las respuestas que te queden:");
        for (String category : PlayerAnswers.CATEGORIES) {
            if (parcials.getAnswer(category).isBlank()) {
                System.out.print(GameUI.promptCategory(category, gameLetter));
                String line = scanner.nextLine().trim();
                parcials.setAnswer(category, line);
            }
        }
        return parcials;
    }

    private void waitingRound() {
        char oldLetter = gameLetter;
        while (connected && gameLetter == oldLetter) {
            sleepMs(100);
        }
    }

    private void waitForResults() {
        sleepMs(3000);
    }

    private void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void send(Message msg) {
        if (!connected || output == null) return;
        try {
            output.writeObject(msg);
            output.flush();
            output.reset();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error al enviar mensaje al servidor", e);
            connected = false;
        }
    }

    private void end() {
        if (connected) {
            send(new Message(MessageType.DISCONNECT, playerName));
            connected = false;
        }
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Error al cerrar el socket", e);
            }
        }
        LOG.info("Cliente cerrado.");
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setStopCalled(boolean stopCalled) {
        this.stopCalled = stopCalled;
    }

    public void startRound(char letter) {
        this.gameLetter = letter;
    }
}
