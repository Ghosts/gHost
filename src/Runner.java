import gHost.Repository;
import gHost.Server;

class Runner implements Repository {
    public static void main(String[] args) {
        Server server = new Server();
        int port = 80; // Default port
        /*Add Working Directories. */
        /* Directory of resource files (pages, images, scripts, etc.)*/
        /* !Important, make sure you either pass a directory as an argument, or change the "a" variable here. */
        String a = Runner.class.getProtectionDomain().getCodeSource().getLocation().toString();
        a = a.replace("file:/","");
        a = a.replace("/out/production/Server/","/resources/");
        System.out.println(a);
        directories.put("root", a);
        /* Directory of pages to serve, may be part of resources. */
        directories.put("pages", a);

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
