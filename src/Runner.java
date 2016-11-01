import gHost.Repository;
import gHost.Server;

import java.util.ArrayList;

class Runner implements Repository {
    public static void main(String[] args) {
        Server server = new Server();
        /* Server settings */

        /* Directory of resource files (pages, images, scripts, etc.)*/
        /* !Important, make sure you either pass a directory as an argument, or change the "d" variable here. */
        String d = Runner.class.getProtectionDomain().getCodeSource().getLocation().toString();
        d = d.replace("file:/","");
        d = d.replace("/out/production/gHost/", "/resources/");
        /*Add Working Directories. */
        directories.put("root", d);
        /* Pages are in my root directory, this is left as blank - no subdirectory needed. */
        directories.put("pages", "");

        /*Set up Routes - not case sensitive by default. */
        routes.put("/","index");
        routes.put("/index","index");
        routes.put("/home","index");

        /* Set Up Graves */
        graves.put("Phantom_Check","Phantom Dynamics are working correctly.");

        switch (args.length) {
            /* Two arguments - port and rootDirectory*/
            case 2:
                int port = Integer.parseInt(args[0]);
                directories.put("root",args[1]);
                server.startServer(port);
                break;
            default:
                server.startServer();
                break;
        }
    }
}
