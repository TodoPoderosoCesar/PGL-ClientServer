package net.salesianos.server;

import net.salesianos.common.Message;
import net.salesianos.common.MessageType;
import net.salesianos.common.PlayerAnswers;
import net.salesianos.utils.EncryptedObjectInputStream;
import net.salesianos.utils.EncryptedObjectOutputStream;
import net.salesianos.utils.SecureManager;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClientHandler implements Runnable {

    private static final Logger LOG = Logger.getLogger(ClientHandler.class.getName());
    private final Socket socket;
    private final GameRoom room;

    // Streams cifrados en lugar de los ObjectOutputStream/ObjectInputStream originales
    private EncryptedObjectOutputStream output;
    private EncryptedObjectInputStream input;

    private String playerName;

    public ClientHandler(Socket socket, GameRoom room) {
        this.socket = socket;
        this.room = room;
    }

    @Override
    public void run() {
        try {
            // Crear SecureManager (lee la clave AES del fichero "secret")
            SecureManager secureManager = new SecureManager();

            // Inicializar los streams cifrados
            output = new EncryptedObjectOutputStream(socket.getOutputStream(), secureManager);
            input  = new EncryptedObjectInputStream(socket.getInputStream(), secureManager);

            LOG.info("Nueva conexión cifrada desde " + socket.getRemoteSocketAddress());

            listeningLoop();

        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error al abrir streams de " + socket.getRemoteSocketAddress(), e);
        } finally {
            finish();
        }
    }

    private void listeningLoop() {
        try {
            while (!socket.isClosed()) {
                Message msg = (Message) input.readObject();
                messageInterpreter(msg);
            }
        } catch (EOFException | SocketException e) {
            LOG.info("Cliente desconectado: " + playerNameOSocket());
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error de E/S con " + playerNameOSocket(), e);
        } catch (ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "Mensaje desconocido de " + playerNameOSocket(), e);
        }
    }

    private void messageInterpreter(Message msg) {
        LOG.fine("Recibido de " + playerNameOSocket() + ": " + msg);

        switch (msg.getType()) {
            case JOIN:
                joinInterpreter(msg);
                break;

            case STOP_CALLED:
                if (playerName != null) {
                    room.handleStop(playerName);
                }
                break;

            case SUBMIT_ANSWERS:
                submitInterpreter(msg);
                break;

            case DISCONNECT:
                LOG.info(playerNameOSocket() + " solicitó desconexión limpia.");
                finish();
                break;

            case READY_NEXT_ROUND:
                room.playerReady();
                break;

            default:
                LOG.warning("Tipo de message inesperado del cliente: " + msg.getType());
                send(new Message(MessageType.ERROR, "Servidor",
                        "Tipo de message no reconocido: " + msg.getType()));
        }
    }

    private void joinInterpreter(Message msg) {
        playerName = msg.getPayloadAsString();

        if (playerName == null || playerName.isBlank()) {
            send(new Message(MessageType.ERROR, "Servidor",
                    "El name de player no puede estar vacío."));
            finish();
            return;
        }

        LOG.info("JOIN recibido de: " + playerName);
        room.joinPlayer(this, playerName);
    }

    private void submitInterpreter(Message msg) {
        if (!(msg.getPayload() instanceof PlayerAnswers)) {
            send(new Message(MessageType.ERROR, "Servidor",
                    "Payload de SUBMIT_ANSWERS inválido."));
            return;
        }
        PlayerAnswers respuestas = (PlayerAnswers) msg.getPayload();
        room.getAnswers(respuestas);
    }

    public synchronized void send(Message message) {
        if (socket.isClosed() || output == null) {
            return;
        }
        try {
            output.writeObject(message);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "No se pudo enviar message a " + playerNameOSocket(), e);
        }
    }

    private void finish() {
        if (socket.isClosed()) {
            return;
        }
        try {
            socket.close();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error al cerrar socket de " + playerNameOSocket(), e);
        } finally {
            room.removePlayer(this);
            LOG.info("Handler cerrado: " + playerNameOSocket());
        }
    }

    private String playerNameOSocket() {
        return playerName != null ? playerName
                : socket.getRemoteSocketAddress().toString();
    }

    public String getPlayerName() {
        return playerName;
    }
}
