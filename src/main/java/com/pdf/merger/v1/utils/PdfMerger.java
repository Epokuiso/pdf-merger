package com.pdf.merger.v1.utils;

import java.util.ArrayList;
import java.util.Date;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.io.FileOutputStream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.itextpdf.io.exceptions.IOException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

@Component
public class PdfMerger {
    private final String INVALID_CONTENT_ERROR = "Oops, we detected a file with an invalid content. Please try again.";
    private final String CONVERTING_FILES_ERROR = "Oops, we had a problem managing the files. Try again soon.";
    private final String MANAGING_FILES_ERROR = "Oops, we had a problem managing the files pages. Try again soon.";
    private final String ONLY_ONE_FILE_SENT = "Oops, we only received one file. Please try again.";
    private final String CONTENT_TYPE = "application/pdf";

    @Autowired
    private FileEraser fileEraser;

    public void verifyMultipartFilesContentType(ArrayList<MultipartFile> files) throws ResponseStatusException {
        if (files.size() == 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ONLY_ONE_FILE_SENT);
        }
        files.forEach((file) -> {
            String fileContentType = file.getContentType();

            if (!CONTENT_TYPE.equalsIgnoreCase(fileContentType)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_CONTENT_ERROR);
            }
        });
    }

    private ArrayList<File> convertMultipartFilesToFilesArrayList(ArrayList<MultipartFile> multipartFiles) throws ResponseStatusException {
        ArrayList<File> convertedFiles = new ArrayList<File>();
        for (int numberOfFile = 0; numberOfFile < multipartFiles.size(); numberOfFile++) {
            File convertedFile = new File(multipartFiles.get(numberOfFile).getOriginalFilename());
            try {
                OutputStream outputStream = new FileOutputStream(convertedFile);
                outputStream.write(multipartFiles.get(numberOfFile).getBytes());
                outputStream.close();   
            } catch (Exception exception) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, CONVERTING_FILES_ERROR);
            }
            convertedFiles.add(convertedFile);
        }
        return convertedFiles;
    }

    private void appendFilePagesToPdfDocumentEnd(ArrayList<File> files, PdfDocument document) throws ResponseStatusException {
        for (int numberOfFile = 0, totalFiles = files.size(); numberOfFile < totalFiles; numberOfFile++) {
            try {
                PdfReader pdfReader = new PdfReader(files.get(numberOfFile));
                PdfDocument pdfDocument = new PdfDocument(pdfReader);
                int numberOfPages = pdfDocument.getNumberOfPages();

                pdfDocument.copyPagesTo(1, numberOfPages, document);
                pdfDocument.close();
            }
            catch(Exception exception) {
                System.out.println(exception.getMessage());
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, MANAGING_FILES_ERROR);
            }
        } 
    }

    private File generateMergedDocumentsFile (ArrayList<File> files) throws ResponseStatusException, IOException {
        File temporaryFile;
        String suffix = ".pdf";
        String prefix =  getTimestampForFileName().toString();
        
        try {
            temporaryFile = File.createTempFile(prefix, suffix);
            PdfWriter pdfWriter = new PdfWriter(temporaryFile);
            PdfDocument pdfMergedDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfMergedDocument);

            appendFilePagesToPdfDocumentEnd(files, pdfMergedDocument);
            pdfMergedDocument.close();
            document.close();
        }
        catch(Exception exception) {
            System.out.println("Generate Error");
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, MANAGING_FILES_ERROR);
        }
        finally  {
            fileEraser.eraseFilesFromServer(files);
        }
        return temporaryFile;
    }

    private Timestamp getTimestampForFileName() {
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        
        return timestamp;
    }

    public Resource mergeDocuments(ArrayList<MultipartFile> documents) throws Exception {
        verifyMultipartFilesContentType(documents);
        ArrayList<File> filesArrayList = convertMultipartFilesToFilesArrayList(documents);
        File mergedDocumentsFile = generateMergedDocumentsFile(filesArrayList);
        
        Path path = Paths.get(mergedDocumentsFile.getPath());
        Resource pdfResource = new UrlResource(path.toUri());
        return pdfResource;
    }
}