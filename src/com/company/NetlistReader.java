package com.company;

import java.io.*;

public class NetlistReader {

    public static String openFile(String filePath) throws IOException {
        FileReader fr = new FileReader(filePath);

        try {
            int c;
            StringBuilder builder = new StringBuilder();

            while ((c = fr.read()) != -1) {
                builder.append((char) c);
            }

            return builder.toString();

        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
