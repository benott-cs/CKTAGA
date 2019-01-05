package edu.uri.cs.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * Created by Ben on 7/24/18.
 */
public class FileReaderUtils {
    public static String readFileAsString(String fileName)throws IOException
    {
        String data = new String(Files.readAllBytes(Paths.get(fileName)));
        return data;
    }

    public static void readInStringLinesFromFile(String fileName, List<String> lines) {
        Scanner s = null;
        try {
            s = new Scanner(new File(fileName));
            while (s.hasNext()) {
                lines.add(s.next());
            }
            s.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(String fileName, List<String> data, boolean executable) {
        FileWriter output = null;
        BufferedWriter writeFile = null;
        File directory = new File(getParentDirectoryNameFromFilename(fileName));
        if (!directory.exists()) {
            directory.mkdirs();
        }
        try {
            output = new FileWriter(fileName);
            writeFile = new BufferedWriter(output);
            for (String datum : data) {
                writeFile.write(datum);
                writeFile.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (Objects.nonNull(writeFile)) {
                try {
                    writeFile.flush();
                    writeFile.close();
                    if (executable) {
                        File file = new File(fileName);
                        file.setExecutable(executable);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getFilenameWithNoExtension(String fileName) {
        String ret = fileName;
        if (ret.indexOf(".") > 0)
            ret = ret.substring(0, fileName.lastIndexOf("."));
        return ret;
    }

    public static String getParentDirectoryNameFromFilename(String filename) {
        Path path = Paths.get(filename);
        return path.getParent().toString();
    }
}
