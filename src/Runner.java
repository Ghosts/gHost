import gHost.Repository;
import gHost.Server;

class Runner implements Repository {
    public static void main(String[] args) {
        Server server = new Server();
        int port = 80;
        /* Directory of resource files (pages, images, scripts, etc.)*/

        /* !Important, make sure you either pass a directory as an argument, or change the "d" variable here. */
        String d = Runner.class.getProtectionDomain().getCodeSource().getLocation().toString();
        d = d.replace("file:/","");
        d = d.replace("/out/production/Server/","/resources/");
        /*Add Working Directories. */
        directories.put("root", d);
        /* Pages are in my root directory, this is left as blank - no subdirectory needed. */
        directories.put("pages", "");

        /*Set up Routes - not case sensitive by default. */
        routes.put("/","index");
        routes.put("/index","index");
        routes.put("/home","index");
        /* Make Casesensitive by using Server.caseSensitiveRoutes = true;*/
        switch (args.length) {
            /* Two arguments - port and rootDirectory*/
            case 2:
                port = Integer.parseInt(args[0]);
                directories.put("root",args[1]);
                server.startServer(port);
                break;
            default:
                server.startServer(port);
                break;
        }
    }
}
