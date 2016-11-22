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
        if(isgHostData()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.home") +
                    "/" + ".gHostPersistentData"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split("\\|");
                    graves.put(data[0], data[1]);
                }
                reader.close();
            }
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


    /* Boolean check to see if prior persistent data saves exist. */
    private static boolean isgHostData(){
        try (BufferedReader read = new BufferedReader(new FileReader(new File(System.getProperty("user.home") + "/" + ".gHostPersistentData")))) {
        return true;
        } catch (IOException e) {
            return false;
        }
    }


    /* Will work to serve compressed versions of files to users. Currently pseudo-works on CSS files. */
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

                    createCompressedFiles(file);
                }
            } else if (listOfFile.isDirectory()) {
                compressFiles(listOfFile.toString());
            }
        }
    }

    /* Creates the compressed versions of files for serving to visitors. */
    private void createCompressedFiles(String file) {
        File oldFile = new File(file);
        String oldFileName = oldFile.toString();
        String newFileName = oldFile.toString().replace("." + getExtension(oldFileName), "_comp" + "." + getExtension(oldFileName));
        File newFile = new File(newFileName);
        compress(oldFile, newFile);
    }

    /* Returns the extension of a file based on a path (such as requested URL). */
    public static String getExtension(String filePath) {
        String extension = "";
        int i = filePath.lastIndexOf('.');
        if (i > 0) {
            extension = filePath.substring(i + 1);
        }
        return extension;
    }

    /* Performs the actual file compression. */
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

    /* Replaces one string with another within a file. */
    static void fileSelectReplace(String filePath, String replaceThis, String withThis) {
        try {
            Path path = Paths.get(filePath);
            String content = new String(Files.readAllBytes(path), Charset.defaultCharset());
            content = content.replaceAll(replaceThis, withThis);
            Files.write(path, content.getBytes(Charset.defaultCharset()));
        } catch (Exception e) {
            Logger.log(Level.ERROR, e.toString());
        }
    }
}
