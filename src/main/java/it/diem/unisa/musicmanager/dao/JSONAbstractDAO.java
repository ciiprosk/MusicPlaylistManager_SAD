package it.diem.unisa.musicmanager.dao;

import it.diem.unisa.musicmanager.exception.FilePathException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class JSONAbstractDAO {
        protected void createFileJSON(String filePath) {
            File file = new File(filePath);

            if (!file.exists()) {
                try {
                    file.createNewFile();
                    // Scrive l'array vuoto per Gson
                    Files.write(Paths.get(filePath), "[]".getBytes());

                } catch (IOException e) {
                    throw new FilePathException("Error: File Path not created!");
                }
            }
        }
}
