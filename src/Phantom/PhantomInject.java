package Phantom;

import gHost.ClientHandler;
import gHost.Repository;
import gHost.Loggable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
* Phantom Inject contains the methods which identify and replace grave variables
* into their representations. Injects are limited to basic replacements. Phantom Dynamics
* handles custom grave variables which may require more complex operations.
* */
public class PhantomInject implements Repository, Loggable  {
    private DefaultInjects DefaultInjects = new DefaultInjects();
    private PhantomDynamics PhantomDynamics = new PhantomDynamics();
    synchronized public void injectPage(String pageRequest, PrintWriter clientOutput) {
        File page = new File(directories.get("root") + directories.get("pages") + pageRequest + ".html");
        try
                (
                        FileInputStream in = new FileInputStream(page);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in))
                ) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (String a : DefaultInjects.getRepository().keySet() ) {
                    if (line.contains(a)) {
                        if (a.contains("``")){
                            Pattern p = Pattern.compile("``\\w++");
                            Matcher m = p.matcher(line);
                            while (m.find()) {
                                line = line.replace(m.group(),PhantomDynamics.graveClean(m.group()));
                            }
                            line = line.replaceAll("``\\w++","");
                        }
                        line = StringUtil.selectReplace(line, a, DefaultInjects.getRepository().get(a));
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
