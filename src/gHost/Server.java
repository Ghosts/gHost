package gHost;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
/**
 * gHost.Server: represents the outermost layer of a clients connection to the program.
*/
public class Server implements Loggable {

    /**
     * Start server and allow for outside connections to be routed.
     *
     * @param port The port of the server.
     * @param rootDirectory The location of external files on the computer.
     */
    public void startServer(int port, String rootDirectory) {
        try (ServerSocket server = new ServerSocket(port)) {
            boolean running = true;
            logger.log(Level.INFO, "gHost.Server started on port: " + port);
            while (running) {
                /* Passes output for each method requiring output access, removed need for class variables */
                try {
                    Socket client = server.accept();
                    Runnable clientHandler = new ClientHandler(client, rootDirectory);
                    new Thread(clientHandler).start();

                } catch (IOException e) {
                    //Serves to break out of while, exception throw accomplishes the same task.
                    running = false;
                }
            }
        } catch (IOException e) {
            /* Gracefully avoid program shutdown by attempting new port.*/
            startServer(port + 1, rootDirectory);
        }
    }
}
