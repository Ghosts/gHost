package gHost;

import Phatnom.PhantomInject;
import Phatnom.StringUtil;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * ClientHandler: responsible for the correct routing of client on connect and request.
*/

public class ClientHandler implements Runnable, Loggable, Repository {
    static final AtomicInteger clientCounter = new AtomicInteger(0);
    private final DataHandler DataHandler = new DataHandler();
    private final Socket client;
    private final PhantomInject PhantomInject = new PhantomInject();

    ClientHandler(Socket client) {
        this.client = client;
        String ip = client.getRemoteSocketAddress().toString().replaceAll(":.*", "");
        DataHandler.addAddress(ip);
    }

    /**
     * Implemented from Runnable, called on Thread creation by unique user connection.
     */
    @Override
    public void run() {
        try (
                BufferedReader clientInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter clientOutput = new PrintWriter(client.getOutputStream())
        ) {
            requestHandler(clientInput, clientOutput);
        } catch (IOException e) {
            logger.log(Level.WARNING, "IOException thrown: " + e);
        }
    }

    private void requestHandler(BufferedReader clientInput, PrintWriter clientOutput) throws IOException {
        String inp;
        while ((inp = clientInput.readLine()) != null) {
            if ("".equals(inp)) {
                break;
            } else if (inp.contains("GET")) {
                routeFilter(inp.split(" "), clientOutput);
            } else if (inp.contains("POST")) {
                badRequestHeader(clientOutput);
                return;
            }
        }
    }

    private void routeFilter(String[] request, PrintWriter clientOutput) {
        String[] queries = StringUtil.formatQuery(request);
        String url = request[1];
        /* Catch all external file calls */
        if (url.contains(".")) {
            loadExternalFile(url, clientOutput);
            return;
        }
        if (routes.get(url) != null) {
            loadPage(clientOutput, routes.get(url));
        } else {
            loadNotFound(clientOutput, "404");
        }
    }

    private void loadNotFound(PrintWriter clientOutput, String pageRequest) {
        /* Send a 404 and load the not found page for the client. */
        errorHeader(clientOutput);
        pageLoader(pageRequest, clientOutput);
    }

    private void loadPage(PrintWriter clientOutput, String pageRequest) {
        textHeader(clientOutput, "html");
        pageLoader(pageRequest, clientOutput);
    }

    synchronized private void pageLoader(String pageRequest, PrintWriter clientOutput) {
            /* Perform PhantomInjects before client write*/
            PhantomInject.injectPage(pageRequest,clientOutput);
    }


    public static void textHeader(PrintWriter clientOutput, String type) {
        clientOutput.println(
                "HTTP/1.0 200 OK\r\n" +
                        "Content-Type: text/" + type + "\r\n" +
                        "Connection:\r\n"
        );
        clientOutput.flush();
    }

    public static void badRequestHeader(PrintWriter clientOutput) {
        clientOutput.println(
                "HTTP/1.0 400 Bad Request\r\n" +
                        "Connection: close\r\n"
        );
        clientOutput.flush();
    }

    public static void errorHeader(PrintWriter clientOutput) {
        clientOutput.println(
                "HTTP/1.0 404 Not Found\r\n" +
                        "Connection: close\r\n"
        );
        clientOutput.flush();
    }

    public static void imageHeader(PrintWriter clientOutput, String type) {
        clientOutput.println(
                "HTTP/1.0 200 OK\r\n" +
                        "Content-Type: image/" + type + "\r\n" +
                        "Content-Length:\r\n"
        );
        clientOutput.flush();
    }

    /* Handles the routing for external files.
    Sends proper headers and identifies file extensions. */
    private void loadExternalFile(String fileRequested, PrintWriter clientOutput) {
        String filepath;
        filepath = directories.get("root") + "/" + fileRequested;
        String extension = "";
        int i = filepath.lastIndexOf('.');
        if (i > 0) {
            extension = filepath.substring(i + 1);
        }
        extension = extension.toLowerCase();
        switch (extension) {
            case "ico":
            case "gif":
            case "jpeg":
            case "jpg":
            case "png":
                imageHeader(clientOutput, extension);
                break;
            case "css":
                textHeader(clientOutput, extension);
                break;
            case "js":
                textHeader(clientOutput, extension);
                break;
            default:
                /* Default, assume some form of text. */
                textHeader(clientOutput, extension);
                return;
        }
        try {
            File file = new File(filepath);
            byte[] array = Files.readAllBytes(file.toPath());
            client.getOutputStream().write(array, 0, array.length);
        } catch (IOException e) {
            logger.log(Level.WARNING, e.toString());
        }
    }
}
