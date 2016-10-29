package gHost;

/**
 * DataHandler: responsible for the correct routing of incoming and outgoing data from gHost.Repository.
 */
class DataHandler implements Repository, Loggable {
    /* Adds each new connection to the clients list. */
    synchronized void addAddress(String ipAddress) {
        if (!connectedIPs.containsKey(ipAddress)) {
            connectedIPs.put(ipAddress, ClientHandler.clientCounter.incrementAndGet());
        }
    }

    /* Parses Queries based on schema http://gHost.example/?=example */
    synchronized void processQuery(String[] queries, String pageRequest) {
    }

}

