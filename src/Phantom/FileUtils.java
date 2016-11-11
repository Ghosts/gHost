package Phantom;

import gHost.ClientHandler;
import gHost.Logger.Level;
import gHost.Logger.Logger;
import gHost.Repository;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils implements Repository {

    static String fragmentString(String fileName, Charset encoding) {
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(Paths.get(directories.get("fragments") + fileName + ".html"));
        } catch (IOException e) {
            Logger.log(Level.ERROR, e.toString());
        }
        return new String(encoded, encoding);
    }

    public static void createTempData() throws IOException {
        File data = new File(System.getProperty("user.home") + "/" + ".gHostPersistentData");
        writeTempData(data);
        Logger.log(Level.INFO, "Data file Saved to: " + data.getAbsolutePath());
    }

    private static void writeTempData(File data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(data))) {
            PhantomDynamics phantomDynamics = new PhantomDynamics();
            if (!graves.isEmpty()) {
                for (String s : graves.keySet()) {
                    writer.write(s + "|" + phantomDynamics.graveClean(s) + "\r\n");
                }
            }
        } catch (IOException e) {
            Logger.log(Level.ERROR, e.toString());
        }
    }

    public void compressFiles(String directory) {
        File folder = new File(directory);
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                String file = listOfFile.toString();
                /* Temporarily disabled, only .css trustworthy at the moment. */
                if (file.contains("_comp")) {
                    boolean deleted = listOfFile.delete();
                } else if (/*file.contains(".html")
                        || file.contains(".js")
                        ||*/ file.contains(".css")) {

                    createFiles(file);
                }
            } else if (listOfFile.isDirectory()) {
                compressFiles(listOfFile.toString());
            }
        }
    }

    private void createFiles(String file) {
        File oldFile = new File(file);
        String oldFileName = oldFile.toString();
        String newFileName = oldFile.toString().replace("." + getExtension(oldFileName), "_comp" + "." + getExtension(oldFileName));
        File newFile = new File(newFileName);
        compress(oldFile, newFile);
    }

    private String getExtension(String filePath) {
        String extension = "";
        int i = filePath.lastIndexOf('.');
        if (i > 0) {
            extension = filePath.substring(i + 1);
        }
        return extension;
    }

    private void compress(File initial, File compressed) {
        try
                (
                        FileInputStream in = new FileInputStream(initial);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        PrintWriter writer = new PrintWriter(compressed, "UTF-8")
                ) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace(" ", "");
                writer.write(line);
            }
            defaultInjects.put(initial.getName(), compressed.getName());
        } catch (Exception e) {
            new ClientHandler().loadNotFound();
            Logger.log(Level.WARNING, "IOException thrown: " + e);
        }
    }
}
