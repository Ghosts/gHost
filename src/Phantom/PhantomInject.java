package Phantom;

import gHost.ClientHandler;
import gHost.Repository;
import gHost.Loggable;
import java.io.*;
import java.util.logging.Level;

public class PhantomInject implements Repository, Loggable  {
    private DefaultInjects injects = new DefaultInjects();

    synchronized public void injectPage(String pageRequest, PrintWriter clientOutput) {
        File page = new File(directories.get("root") + directories.get("pages") + pageRequest + ".html");
        try
                (
                        FileInputStream in = new FileInputStream(page);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in))
                ) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (String a : injects.getRepository().keySet() ) {
                    if (line.contains(a)) {
                        line = StringUtil.selectReplace(line, a, injects.getRepository().get(a));
                    }
                }
                clientOutput.println(line);
            }
            clientOutput.println();
            clientOutput.flush();
        } catch (Exception e) {
            new ClientHandler().loadNotFound();
            logger.log(Level.WARNING, "IOException thrown: " + e);
        }
    }
}
