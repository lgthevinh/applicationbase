package org.thingai.base.dao;

import java.io.*;
import java.nio.file.Files;

public class DaoFile {
    private String rootPath;

    public DaoFile(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String readJsonFile(String filePath) throws IOException {
        File file = new File(rootPath, filePath);

        if (!file.exists()) {
            return null;
        }

        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));

        reader.lines().forEach(line -> content.append(line).append("\n"));

        reader.close();

        return content.toString();
    }

    public void writeJsonFile(String filePath, String content) throws IOException {
        File file = new File(rootPath, filePath);

        file.getParentFile().mkdirs();

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(content);
        writer.close();
    }
}
