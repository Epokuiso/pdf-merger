package com.pdf.merger.v1.utils;

import java.io.File;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

@Component
public class FileEraser {
    
    public void eraseFilesFromServer(ArrayList<File> files) {
        files.forEach((file) -> file.delete());
    }
}
