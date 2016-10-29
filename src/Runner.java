import gHost.Database;
import gHost.Server;

class Runner implements Database {
    public static void main(String[] args) {
        Server server = new Server();
        int port = 80; // Default port
        /*Add Working Directories. */
        /* Directory of resource files (pages, images, scripts, etc.)*/
        directories.put("root","TODO Add");
        /* Directory of pages to serve, may be part of resources. */
        directories.put("pages","TODO Add");

        /*Set up Routes*/
        routes.put("/","index");
        routes.put("/index","index");
        routes.put("/home","index");

        switch (args.length) {
            /* Two arguments - port and rootDirectory*/
            case 2:
                port = Integer.parseInt(args[0]);
                directories.put("root",args[1]);
                server.startServer(port, directories.get("root"));
                break;
            default:
                server.startServer(port, directories.get("root"));
        }
    }
}
