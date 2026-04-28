package net.salesianos.server;

import net.salesianos.client.Client;
import net.salesianos.client.GameUI;
import net.salesianos.common.Message;
import net.salesianos.common.MessageType;
import net.salesianos.common.PlayerScore;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerListener implements Runnable {

    private static final Logger LOG = Logger.getLogger(ServerListener.class.getName());

    private final ObjectInputStream input;
    private final Client cliente;

    public ServerListener(ObjectInputStream input, Client client) {
        this.input = input;
        this.cliente  = client;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Message msg = (Message) input.readObject();
                handleMessage(msg);

                if (msg.getType() == MessageType.GAME_OVER
                        || msg.getType() == MessageType.DISCONNECT) {
                    break;
                }
            }
        } catch (EOFException | SocketException e) {

            if (!cliente.isConnected()) {
                return;
            }
            GameUI.printDisconnection();
            cliente.setConnected(false);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error de E/S en ServerListener", e);
            cliente.setConnected(false);
        } catch (ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "Mensaje desconocido recibido del servidor", e);
        }
    }

    private void handleMessage(Message msg) {
        LOG.fine("Recibido del servidor: " + msg);

        switch (msg.getType()) {

            case JOINED_OK:
                GameUI.printEntranceToRoom(msg.getPayloadAsString());
                break;

            case WAITING_PLAYERS:
                GameUI.printWaitingForPlayers(msg.getPayloadAsString());
                break;

            case START_ROUND:
                char letra = msg.getPayloadAsString().charAt(0);
                cliente.startRound(letra);
                GameUI.printRoundStart(letra);
                break;

            case STOP_CALLED:
                handleStop(msg);
                break;

            case ROUND_RESULT:
                @SuppressWarnings("unchecked")
                List<PlayerScore> roundScore = (List<PlayerScore>) msg.getPayload();
                GameUI.printResults(roundScore, false);
                break;

            case GAME_OVER:
                @SuppressWarnings("unchecked")
                List<PlayerScore> marcadorFinal = (List<PlayerScore>) msg.getPayload();
                GameUI.printGameFinished(marcadorFinal);
                cliente.setConnected(false);
                break;

            case INFO:
                GameUI.printInfo(msg.getPayloadAsString());
                break;

            case ERROR:
                GameUI.printErrorMessage(msg.getPayloadAsString());
                break;

            default:
                LOG.warning("Tipo de mensaje no esperado del servidor: " + msg.getType());
        }
    }

    private void handleStop(Message msg) {
        String whoStopped = msg.getPayloadAsString();
        boolean wasMe = whoStopped.equals(cliente.getPlayerName());
        GameUI.printStop(whoStopped, wasMe);

        if (!wasMe) {
            cliente.setStopCalled(true);
        }
    }
}

