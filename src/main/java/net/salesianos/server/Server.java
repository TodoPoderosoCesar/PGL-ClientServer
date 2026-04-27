package net.salesianos.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());
    public static final int DEFAULT_PORT = 12345;

    public static void main(String[] args) {
        int port = parsePort(args);
        new Server().run(port);
    }

    public void run(int port) {
        GameRoom room = new GameRoom();

        ExecutorService pool = Executors.newCachedThreadPool();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Apagando server...");
            pool.shutdownNow();
        }));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            LOG.info("Server STOP Online escuchando en el puerto " + port);
            LOG.info("Esperando jugadores (mínimo " + GameRoom.MIN_JUGADORES + ")...");

            while (!serverSocket.isClosed()) {
                try {
                    Socket clienteSocket = serverSocket.accept();
                    LOG.info("Conexión entrante desde: "
                            + clienteSocket.getRemoteSocketAddress());

                    ClientHandler handler = new ClientHandler(clienteSocket, room);
                    pool.execute(handler);

                } catch (IOException e) {
                    if (serverSocket.isClosed()) {
                        break;
                    }
                    LOG.log(Level.WARNING, "Error al aceptar conexión", e);
                }
            }

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "No se pudo abrir el ServerSocket en el puerto " + port, e);
        } finally {
            pool.shutdown();
        }
    }

    private static int parsePort(String[] args) {
        if (args.length > 0) {
            try {
                int port = Integer.parseInt(args[0]);
                if (port < 1 || port > 65535) {
                    LOG.warning("Puerto fuera de rango. Usando " + DEFAULT_PORT);
                    return DEFAULT_PORT;
                }
                return port;
            } catch (NumberFormatException e) {
                LOG.warning("Puerto inválido '" + args[0] + "'. Usando " + DEFAULT_PORT);
            }
        }
        return DEFAULT_PORT;
    }
}
