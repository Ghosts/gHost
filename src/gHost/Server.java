package gHost;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
/**
 * Server: represents the outermost layer of a clients connection to the program.
*/
public class Server implements Loggable, Repository {

    /**
     * Start server and allow for outside connections to be routed.
     */
    public void startServer(int port) {
        try (ServerSocket server = new ServerSocket(port)) {
            boolean running = true;
            logger.log(Level.INFO, "gHost.Server started on port: " + port);
            while (running) {
                /* Passes output for each method requiring output access, removed need for class variables */
                try {
                    Socket client = server.accept();
                    Runnable clientHandler = new ClientHandler(client);
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    //Serves to break out of while, exception throw accomplishes the same task.
                    running = false;
                }
            }
        } catch (IOException e) {
            /* Gracefully avoid program shutdown by attempting new port. !~ Make sure you check the correct port is used. */
            startServer(port + 1);
        }
    }
}
