package it.diem.unisa.musicmanager.dao;

import it.diem.unisa.musicmanager.exception.FilePathException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


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

                }
            } catch (Exception e) {
                throw new FilePathException("Error: File Path not created!");
            }
        }
}
