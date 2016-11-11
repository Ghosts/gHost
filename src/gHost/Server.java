package gHost;

import Phantom.FileUtils;
import gHost.Logger.Level;
import gHost.Logger.Logger;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * Server: represents the outermost layer of a clients connection to the program.
*/
public class Server implements Repository {
    public static Socket client;

    /* Server Settings */
    public static boolean caseSensitiveRoutes; //Allows URL requests to be of any capitalization
    public static boolean debugMode; //Enables additional logging information for debugging
    public static boolean fileCompressor; //Reduces file size of HTML, CSS & JavaScript files
    public static boolean enablePhantom; //Enable or disable Phantom functionality (Fragments, compression, default injects, etc.)
    public static boolean enableGraves; //Enable or disable Graves (custom defined variables)
    public static boolean persistentData; //Save and load Graves and repository data from file

    /* Default to port 80. */
    public void startServer(){
        startServer(80);
    }
    /**
     * Start server and allow for outside connections to be routed.
     */
    public void startServer(int port) {
        try (ServerSocket server = new ServerSocket(port)) {
            boolean running = true;
            Logger.log(Level.INFO, "gHost.Server started on port: " + port);
            if(debugMode){directories.forEach((k,v) -> Logger.log(Level.INFO,"Directory: "+ k + " Path: " + v));}
            if(fileCompressor){new FileUtils().compressFiles(directories.get("resources"));}
            while (running) {
                /* Passes output for each method requiring output access, removed need for class variables */
                try {
                    client = server.accept();
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
