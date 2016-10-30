package gHost;

import java.io.PrintWriter;
import java.util.HashMap;

/**
 * The Repository interface holds data structures accessible
 * to classes that require them. By default the gHost.Repository will
 * store connected IP's, routes, and directories.
 */
public interface Repository {
    /* Stores connection information for the logger. */
    HashMap<String, Integer> connectedIPs = new HashMap<>();
    /* Contains route information for pages or APIs. */
    HashMap<String,String> routes = new HashMap<>();
    /* Sets main directories, such as root, resources, etc.*/
    HashMap<String,String> directories = new HashMap<>();

}
