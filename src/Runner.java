import Phantom.DefaultInjects;
import gHost.Repository;
import gHost.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Runner implements Repository {
    public static void main(String[] args) {
        Server server = new Server();

        /* Server settings */
        //IMPORTANT: These settings may not be 100% stable. Please refer to documentation.
        Server.caseSensitiveRoutes = true; //Allows URL requests to be of any capitalization
        Server.debugMode = false; //Enables additional logging information for debugging
        Server.fileCompressor = false; //Reduces file size of HTML, CSS & JavaScript files
        Server.enablePhantom = true; //If disabled, neither Phantom Defaults nor Grave Variables will work.
        Server.enableGraves = true; //If disabled, Phantom Defaults will work, but Grave variables will not.

        String d = Runner.class.getProtectionDomain().getCodeSource().getLocation().toString();
        d = d.replace("file:/","");
        d = d.replace("/out/production/gHost/", "/");

        /*Add Working Directories. */
        /* Root directory is for where ALL gHost files are located. */
        directories.put("root", d);
        d = d + "resources/";
        /* Resources should be where images, js, css, html, etc. are located. */
        directories.put("resources", d);
        /* If pages are not saved within resources, add the subdirectory path to append to resources */
        directories.put("pages","");
        /* Directory for location of fragments */
        directories.put("fragments", directories.get("root") + "src/Phantom/Fragments/");

        /*Set up Routes - not case sensitive by default. */
        routes.put("/","index");
        routes.put("/index","index");
        routes.put("/home","index");

        /* Set Up Graves */
        graves.put("Phantom_Check","Phantom Dynamics are working correctly.");

        /* Start server after settings, routes, graves, etc. */
        switch (args.length) {
            /* Two arguments - port and rootDirectory*/
            case 2:
                int port = Integer.parseInt(args[0]);
                directories.put("root",args[1]);
                server.startServer(port);
                break;
            case 3:
                port = Integer.parseInt(args[0]);
                directories.put("root", args[1]);
                directories.put("resources", args[2]);
                server.startServer(port);
                break;
            default:
                server.startServer();
                break;
        }
    }
}
