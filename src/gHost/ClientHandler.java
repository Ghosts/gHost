package gHost;

import Phatnom.StringUtil;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * gHost.ClientHandler: responsible for the correct routing of client on connect and request.
*/

class ClientHandler implements Runnable, Loggable, Database {
    /**
     * Number of unique client connections during server operation.
     */
    static final AtomicInteger clientCounter = new AtomicInteger(0);
    private final DataHandler DataHandler = new DataHandler();

    /**
     * The connection to the client's device.
     */
    private final Socket client;

    /**
     * The remote ip address of the client's device.
     */
    private final String ip;

    /**
     * The location of the program's file store.
     */
    private final String rootDirectory;


    /**
     * gHost.ClientHandler constructor.
     *
     * @param client        The connection to the client represented by a Socket.
     * @param rootDirectory The location of the program's file store.
     */
    ClientHandler(Socket client, String rootDirectory) {
        this.client = client;
        this.rootDirectory = rootDirectory;
        //Save the client's ip for later use.
        this.ip = client.getRemoteSocketAddress().toString().replaceAll(":.*", "");
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

    /**
     * Parses the user's request and forwards to the route handler on GET request detected.
     *
     * @param clientInput  The data received from the client.
     * @param clientOutput The pipe to the client.
     * @throws IOException Thrown when no clientInput exists.
     */
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

    /**
     * Determines which page to return to client based on parsed request.
     *
     * @param request      An array of client request data.
     * @param clientOutput The pipe to the client.
     */
    private void routeFilter(String[] request, PrintWriter clientOutput) {
        String[] queries = StringUtil.formatQuery(request);
        String url = request[1];
        /* Catch all external file calls
         * TODO abstract into better file call checks. */
        if (url.contains(".")) {
            loadExternalFile(url, clientOutput);
            return;
        }
        if(routes.get(url) != null){
            loadPage(clientOutput,routes.get(url));
        } else {
            loadNotFound(clientOutput,"404");
        }
    }


    /**
     * Routes request and query data to the correct method to be sent to user.
     *
     * @param clientOutput The pipe to the client.
     * @param pageRequest  The name of the page client has requested.
     * @param queries      The list of queries to be handled.
     */
    synchronized private void loadDynamic(PrintWriter clientOutput, String pageRequest, String[] queries) {
        /* Insert, if any, query data into the database through gHost.DataHandler. */
        HashMap<String, String> updates = null;
        boolean dynamic = queries.length > 0;
        try {
            DataHandler.processQuery(queries, pageRequest);
            switch (pageRequest) {
                default:
                    break;
            }
        } finally {
            /* Send an ok and load the new order page for the client. */
            textHeader(clientOutput, "html");
            /* If no queries are present, load page normally; such as on first load. */
            if (!dynamic) {
                pageLoader(pageRequest, clientOutput, "%STATUS%", "New");
            } else {
            /* Will replace locations on the page with relevant information on a new order. */
                pageLoader(pageRequest, clientOutput, updates);
            }
        }
    }

    /**
     * Sends 404 error code header to client in case of file not found.
     *
     * @param clientOutput The pipe to the client.
     * @param pageRequest  The page requested by the client.
     */
    private void loadNotFound(PrintWriter clientOutput, String pageRequest) {
        /* Send a 404 and load the not found page for the client. */
        errorHeader(clientOutput);
        pageLoader(pageRequest, clientOutput);
    }

    /**
     * Sends static html and 200 ok code to the client.
     *
     * @param clientOutput The pipe to the client.
     * @param pageRequest  The page requested by the client.
     */
    private void loadPage(PrintWriter clientOutput, String pageRequest) {
        /* Send an ok and load the new order page for the client. */
        textHeader(clientOutput, "html");
        pageLoader(pageRequest, clientOutput);
    }


    /**
     * Sends static html to the client.
     *
     * @param pageRequest  The page requested by the client.
     * @param clientOutput The pipe to the client.
     */
    synchronized private void pageLoader(String pageRequest, PrintWriter clientOutput) {
        File page = new File(directories.get("pages") + pageRequest + ".html");
        try
                (
                        FileInputStream in = new FileInputStream(page);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in))
                ) {
            String line;
            while ((line = reader.readLine()) != null) {
                clientOutput.println(line);
            }
            clientOutput.println();
            clientOutput.flush();
        } catch (Exception e) {
            errorHeader(clientOutput);
            logger.log(Level.WARNING, "Exception thrown: " + e);
        }
    }

    /**
     * Sends dynamic html to the client.
     *
     * @param pageRequest  The page requested by the client.
     * @param clientOutput The pipe to the client.
     * @param replacements A map of dynamic replacements to be made.
     */
    synchronized private void pageLoader(String pageRequest, PrintWriter clientOutput, HashMap<String, String> replacements) {
        File page = new File(rootDirectory + "/pages/" + pageRequest + ".html");
        try
                (
                        FileInputStream in = new FileInputStream(page);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in))
                ) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (String a : replacements.keySet()) {
                    if (line.contains(a)) {
                        line = StringUtil.selectReplace(line, a, replacements.get(a));
                    }
                }
                clientOutput.println(line);
            }
            clientOutput.println();
            clientOutput.flush();
        } catch (Exception e) {
            errorHeader(clientOutput);
            logger.log(Level.WARNING, "IOException thrown: " + e);
        }
    }

    /**
     * Sends dynamic html to the client.
     *
     * @param pageRequest  The page requested by the client.
     * @param clientOutput The pipe to the client.
     * @param replaceThis  The tag to be replaced.
     * @param withThis     The string to replace the tag.
     */
    /* Overloaded pageLoader that will replace a single tag to a different value. */
    synchronized private void pageLoader(String pageRequest, PrintWriter clientOutput, String replaceThis, String withThis) {
        File page = new File(rootDirectory + "resources/pages/" + pageRequest + ".html");
        try
                (
                        FileInputStream in = new FileInputStream(page);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in))
                ) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(replaceThis)) {
                    line = StringUtil.selectReplace(line, replaceThis, withThis);
                }
                clientOutput.println(line);
            }
            clientOutput.println();
            clientOutput.flush();
        } catch (Exception e) {
            errorHeader(clientOutput);
            logger.log(Level.WARNING, "Exception thrown: " + e);
        }
    }

    private void textHeader(PrintWriter clientOutput, String type) {
        clientOutput.println(
                "HTTP/1.0 200 OK\r\n" +
                        "Content-Type: text/" + type + "\r\n" +
                        "Connection:\r\n"
        );
        clientOutput.flush();
    }

    private void badRequestHeader(PrintWriter clientOutput) {
        clientOutput.println(
                "HTTP/1.0 400 Bad Request\r\n" +
                        "Connection: close\r\n"
        );
        clientOutput.flush();
    }

    private void errorHeader(PrintWriter clientOutput) {
        clientOutput.println(
                "HTTP/1.0 404 Not Found\r\n" +
                        "Connection: close\r\n"
        );
        clientOutput.flush();
    }

    private void imageHeader(PrintWriter clientOutput, String type) {
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
        filepath = rootDirectory + "/" + fileRequested;
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
                textHeader(clientOutput, "css");
                break;
            case "js":
                textHeader(clientOutput, "js");
                break;
            default:
                errorHeader(clientOutput);
                return;
        }
        try {
            byte[] array = Files.readAllBytes(new File(filepath).toPath());
            client.getOutputStream().write(array, 0, array.length);
        } catch (FileNotFoundException e) {
            errorHeader(clientOutput);
            logger.log(Level.WARNING, e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            logger.log(Level.WARNING, e.toString());
            e.printStackTrace();
        }
    }
}
