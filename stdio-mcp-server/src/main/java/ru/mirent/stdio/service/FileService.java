package ru.mirent.stdio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class FileService {
    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

    public String read(String path) {
        try {
            String result = "";
            FileInputStream fileInputStream = new FileInputStream(path);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }

            LOG.info("Файла открыт по пути: {}", path);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
