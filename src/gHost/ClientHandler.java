package gHost;

import Phantom.DefaultInjects;
import Phantom.PhantomInject;
import Phantom.StringUtils;
import gHost.Logger.Level;
import gHost.Logger.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClientHandler: responsible for the correct routing of client on connect and request.
 */

public class ClientHandler implements Runnable, Repository {
    static final AtomicInteger clientCounter = new AtomicInteger(0);
    private final Socket client;
    private final PhantomInject PhantomInject = new PhantomInject();
    private PrintWriter clientOutput = null;

    ClientHandler(Socket client) {
        new DefaultInjects(); // Populate Default Injects -- TODO: find better instantiation spot.
        this.client = client;
        String ip = (((InetSocketAddress) Server.client.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
        gHost.DataHandler dataHandler = new DataHandler();
        dataHandler.addAddress(ip);
    }

    /* Default Constructor ONLY used for clientOutput write methods. */
    public ClientHandler() {
        this(Server.client);
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
            this.clientOutput = clientOutput;
            requestHandler(clientInput);
        } catch (IOException e) {
            Logger.log(Level.WARNING, "IOException thrown: " + e);
        }
    }

    private void requestHandler(BufferedReader clientInput) throws IOException {
        String inp;
        while ((inp = clientInput.readLine()) != null) {
            if ("".equals(inp)) {
                break;
            } else if (inp.contains("GET")) {
                routeFilter(inp.split(" "));
            } else if (inp.contains("POST")) {
                badRequestHeader();
                return;
            }
        }
    }

    private void routeFilter(String[] request) {
        String[] queries = StringUtils.formatQuery(request);
        if (queries.length != 0) {
            System.out.println(queries[0]);
        }
        String url = request[1];
        if (Server.debugMode) {
            Logger.log(Level.INFO, "Route Request: " + url);
        }
        if (!Server.caseSensitiveRoutes) {
            url = url.toLowerCase();
        }
        /* Catch all external file calls */
        if (url.contains(".")) {
            loadExternalFile(url);
            return;
        }
        if (routes.get(url) != null) {
            loadPage(routes.get(url));
        } else {
            loadNotFound();
        }
    }

    public void loadNotFound() {
        /* Send a 404 and load the not found page for the client. */
        errorHeader();
        pageLoader("404");
    }

    private void loadPage(String pageRequest) {
        textHeader("html");
        pageLoader(pageRequest);
    }

    synchronized private void pageLoader(String pageRequest) {
        /* Perform PhantomInjects before client write*/
        PhantomInject.injectPage(pageRequest, clientOutput);
    }


    private void textHeader(String type) {
        clientOutput.println(
                "HTTP/1.0 200 OK\r\n" +
                        "Content-Type: text/" + type + "\r\n" +
                        "Connection: Powered by gHost v0.05b.\r\n"
        );
        clientOutput.flush();
    }

    private void badRequestHeader() {
        clientOutput.println(
                "HTTP/1.0 400 Bad Request\r\n" +
                        "Connection: close\r\n"
        );
        clientOutput.flush();
    }

    private void errorHeader() {
        clientOutput.println(
                "HTTP/1.0 404 Not Found\r\n" +
                        "Connection: close\r\n"
        );
        clientOutput.flush();
    }

    private void imageHeader(String type) {
        clientOutput.println(
                "HTTP/1.0 200 OK\r\n" +
                        "Content-Type: image/" + type + "\r\n" +
                        "Content-Length:\r\n"
        );
        clientOutput.flush();
    }

    /* Handles the routing for external files.
    Sends proper headers and identifies file extensions. */
    private void loadExternalFile(String fileRequested) {
        String filepath;
        filepath = directories.get("resources") + "/" + fileRequested;
        if(fileRequested.contains("/dynamic.js")){
            filepath = directories.get("dynamics") + fileRequested;
        }
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
                imageHeader(extension);
                break;
            case "css":
                textHeader(extension);
                break;
            case "js":
                textHeader(extension);
                break;
            default:
                /* Default, assume some form of text. */
                textHeader(extension);
                return;
        }
        try {
            File file = new File(filepath);
            byte[] array = Files.readAllBytes(file.toPath());
            client.getOutputStream().write(array, 0, array.length);
        } catch (IOException e) {
            Logger.log(Level.ERROR, e.toString());
        }
    }
}
