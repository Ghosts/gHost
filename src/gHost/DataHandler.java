package gHost;

import java.util.logging.Level;

/**
 * DataHandler: responsible for the correct routing of incoming and outgoing data from gHost.Repository.
 */
class DataHandler implements Repository, Loggable {
    /* Adds each new connection to the clients list. */
    synchronized void addAddress(String ipAddress) {
        if (!connectedIPs.containsKey(ipAddress)) {
            connectedIPs.put(ipAddress, ClientHandler.clientCounter.incrementAndGet());
            if(Server.debugMode){logger.log(Level.INFO,"New Connection From: " + ipAddress);}
        }
    }

    /* Use queries based on what is present */
    synchronized void processQuery(String[] queries, String pageRequest) {

    }

}

