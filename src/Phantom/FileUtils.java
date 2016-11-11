package Phantom;

import gHost.ClientHandler;
import gHost.Logger.Level;
import gHost.Logger.Logger;
import gHost.Repository;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static void creategHostData() throws IOException {
        File data = new File(System.getProperty("user.home") + "/" + ".gHostPersistentData");
        writegHostData(data);
        Logger.log(Level.INFO, "Data file Saved to: " + data.getAbsolutePath());
    }

    public static void loadgHostData() throws IOException {
        try(    BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.home") +
        "/" + ".gHostPersistentData"))) {
            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] data = line.split("\\|");
                graves.put(data[0],data[1]);
            }
            reader.close();
        }
    }

    private static void writegHostData(File data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(data))) {
            PhantomGraves phantomGraves = new PhantomGraves();
            if (!graves.isEmpty()) {
                for (String s : graves.keySet()) {
                    writer.write(s + "|" + phantomGraves.graveClean(s) + "\r\n");
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
                   listOfFile.delete();
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

    static void fileSelectReplace(String filePath, String replaceThis, String withThis) {
        try {
            System.out.println(filePath + "<<<<<<<<<<");
            Path path = Paths.get(filePath);
            String content = new String(Files.readAllBytes(path), Charset.defaultCharset());
            content = content.replaceAll(replaceThis, withThis);
            Files.write(path, content.getBytes(Charset.defaultCharset()));
        } catch (Exception e) {
            Logger.log(Level.ERROR, e.toString());
        }
    }
}
