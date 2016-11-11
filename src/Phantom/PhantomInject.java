package Phantom;

import gHost.ClientHandler;
import gHost.Logger.Level;
import gHost.Logger.Logger;
import gHost.Repository;
import gHost.Server;

import java.io.*;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* Phantom Inject contains the methods which identify and replace grave variables
* into their representations. Injects are limited to basic replacements. Phantom Dynamics
* handles custom grave variables which may require more complex operations.
* */
public class PhantomInject implements Repository {
    private PhantomDynamics PhantomDynamics = new PhantomDynamics();

    synchronized public void injectPage(String pageRequest, PrintWriter clientOutput) {
        File page = new File(directories.get("resources") + directories.get("pages") + pageRequest + ".html");
        try
                (
                        FileInputStream in = new FileInputStream(page);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in))
                ) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (Server.enablePhantom) {
                    for (String a : defaultInjects.keySet()) {
                        if (Server.enableGraves) {
                            if (a.contains("``")) {
                                Pattern p = Pattern.compile("``\\w++");
                                Matcher m = p.matcher(line);
                                while (m.find()) {
                                    line = line.replace(m.group(), (String) PhantomDynamics.graveClean(m.group()));
                                }
                            }
                        }
                        if (line.contains("<%Fragment%>")) {
                            String fileName = line.replace("<%Fragment%>", "");
                            fileName = fileName.replaceAll("\\s", "");
                            line = FileUtils.fragmentString(fileName, Charset.defaultCharset());
                        }
                        line = line.replace(a, defaultInjects.get(a));
                    }
                }
                clientOutput.println(line);
            }
            clientOutput.println();
        } catch (Exception e) {
            new ClientHandler().loadNotFound();
            Logger.log(Level.WARNING, "IOException thrown: " + e);
        } finally {
            clientOutput.flush();
        }
    }
}
