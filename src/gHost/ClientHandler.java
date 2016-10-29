package gHost;

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

class ClientHandler implements Runnable, Loggable, Repository {
    static final AtomicInteger clientCounter = new AtomicInteger(0);
    private final DataHandler DataHandler = new DataHandler();
    private final Socket client;
    private final String rootDirectory;

    ClientHandler(Socket client, String rootDirectory) {
        this.client = client;
        this.rootDirectory = rootDirectory;
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
        if(routes.get(url) != null){
            loadPage(clientOutput,routes.get(url));
        } else {
            loadNotFound(clientOutput,"404");
        }
    }

    synchronized private void loadDynamic(PrintWriter clientOutput, String pageRequest, String[] queries) {
        HashMap<String, String> updates = null;
        boolean dynamic = queries.length > 0;
        try {
            DataHandler.processQuery(queries, pageRequest);
            switch (pageRequest) {
                default:
                    break;
            }
        } finally {
            textHeader(clientOutput, "html");
            /* If no queries are present, load page normally; such as on first load. */
            if (!dynamic) {
                pageLoader(pageRequest, clientOutput, "%STATUS%", "New");
            } else {
                pageLoader(pageRequest, clientOutput, updates);
            }
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
