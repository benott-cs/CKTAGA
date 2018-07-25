package edu.uri.cs.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Ben on 7/24/18.
 */
public class FileReaderUtils {
    public static String readFileAsString(String fileName)throws IOException
    {
        String data = new String(Files.readAllBytes(Paths.get(fileName)));
        return data;
    }
}
