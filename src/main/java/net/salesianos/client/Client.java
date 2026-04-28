package net.salesianos.client;

import net.salesianos.common.Message;
import net.salesianos.common.MessageType;
import net.salesianos.common.PlayerAnswers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static final Logger LOG = Logger.getLogger(Client.class.getName());
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 12345;

    private volatile boolean connected = false;
    private volatile boolean stopCalled = false;
    private volatile char gameLetter;
    private volatile boolean roundResultReceived = false;

    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    private String playerName;

    private final BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = DEFAULT_PORT;

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

        // Hilo único que lee stdin y mete líneas en la cola.
        // Es daemon para que no impida que la JVM cierre.
        Thread stdinReader = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                while ((line = br.readLine()) != null) {
                    inputQueue.put(line);
                }
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        stdinReader.setDaemon(true);
        stdinReader.start();

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
        socket = new Socket(host, port);
        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());
        connected = true;

        LOG.info("Conectado a " + host + ":" + port);

        Thread listenerThread = new Thread(new ServerListener(input, this));
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void gameLoop() {
        // Nombre del jugador — bloqueamos normalmente antes de conectar al juego
        System.out.print("Introduce tu nombre: ");
        playerName = readLineBlocking().trim();
        while (playerName.isBlank()) {
            System.out.print("El nombre no puede estar vacío. Introduce tu nombre: ");
            playerName = readLineBlocking().trim();
        }
        send(new Message(MessageType.JOIN, playerName, playerName));

        while (connected) {
            stopCalled = false;
            waitingRound();

            if (!connected) break;

            PlayerAnswers answers = getAllAnswers();

            send(new Message(MessageType.SUBMIT_ANSWERS, playerName, answers));

            waitForResults();
        }
    }

    private String readLineBlocking() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    private String readLineInterruptible() {
        while (true) {
            try {
                String line = inputQueue.poll(100, TimeUnit.MILLISECONDS);
                if (line != null) {
                    return line;          // el usuario escribió algo
                }
                if (stopCalled) {
                    return null;          // STOP externo, no hay input pendiente
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    private PlayerAnswers getAllAnswers() {
        PlayerAnswers answers = new PlayerAnswers(playerName, gameLetter);
        boolean playerStop = false;

        for (String category : PlayerAnswers.CATEGORIES) {

            if (stopCalled) {
                GameUI.printForcedStop();
                break;
            }

            System.out.print(GameUI.promptCategory(category, gameLetter));

            String line = readLineInterruptible();

            if (line == null) {
                // STOP externo llegó mientras esperábamos
                GameUI.printForcedStop();
                break;
            }

            line = line.trim();

            // Si llegó un STOP externo justo cuando el usuario pulsaba Enter,
            // ignoramos lo que haya escrito y paramos.
            if (stopCalled) {
                GameUI.printForcedStop();
                break;
            }

            if (line.equalsIgnoreCase("STOP")) {
                playerStop = true;
                send(new Message(MessageType.STOP_CALLED, playerName, playerName));
                break;
            }

            answers.setAnswer(category, line);
        }

        if (playerStop && !stopCalled) {
            answers = completeAnswersAfterStop(answers);
        }

        return answers;
    }

    private PlayerAnswers completeAnswersAfterStop(PlayerAnswers parcials) {
        System.out.println("  Completa las respuestas que te queden:");
        for (String category : PlayerAnswers.CATEGORIES) {
            if (parcials.getAnswer(category).isBlank()) {

                // Si mientras completamos el otro jugador también para, salimos
                if (stopCalled) {
                    break;
                }

                System.out.print(GameUI.promptCategory(category, gameLetter));
                String line = readLineInterruptible();

                if (line == null || stopCalled) {
                    break;
                }

                parcials.setAnswer(category, line.trim());
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
        roundResultReceived = false;
        while (connected && !roundResultReceived) {
            sleepMs(100);
        }
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

    public void setRoundResultReceived(boolean received) {
        this.roundResultReceived = received;
    }

    public void startRound(char letter) {
        this.gameLetter = letter;
    }
}