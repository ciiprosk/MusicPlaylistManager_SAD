package it.diem.unisa.musicmanager.dao;

import it.diem.unisa.musicmanager.exception.FilePathException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class JSONAbstractDAO {

        protected void createFileJSON(String filePath, String pathFolder) {
            File file = new File(filePath);
            File folder = new File(pathFolder);

            try{
                //controllo che la directory esista
                if(!folder.exists()){
                    folder.mkdirs(); //crea la directory

                }
                if(!file.exists()){
                    file.createNewFile();
                    Files.write(Paths.get(filePath), "[]".getBytes()); // si usa files perché è una lobreria nuova e rende più semplici questi metodi
                    //getbytes si usa peer converire la stringa in byte altrimenti non nsi possono scrivere nel file
                }
            } catch (Exception e) {
                throw new FilePathException("Error: File Path not created!");
            }
        }
}
