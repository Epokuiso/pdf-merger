package com.pdf.merger.v1.services;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.pdf.merger.v1.utils.PdfMerger;

@Service
public class MergerService {

    @Autowired
    private PdfMerger pdfMerger;

    public ResponseEntity<Resource> mergeDocuments(ArrayList<MultipartFile> files) {
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdfMerger.mergeDocuments(files));
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            String errorCode = errorMessage.substring(0, errorMessage.indexOf(" "));
            throw new ResponseStatusException(Integer.parseInt(errorCode), exception.getMessage(), exception);
        }
    }
}
