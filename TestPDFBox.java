package com.example.knowledgesphere;

import java.io.File;
import java.nio.file.Files;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import java.io.ByteArrayOutputStream;

public class TestPDFBox {
    public static void main(String[] args) throws Exception {
        byte[] pdfBytes = Files.readAllBytes(new File("uploads/documents/0d755fca-43a0-4d25-a0b1-4732b523cbfd_CHRM 05(2026)-Annual Medical Health Check-up.pdf").toPath());
        System.out.println("Loaded bytes: " + pdfBytes.length);
        
        for (int i=0; i<1; i++) {
            try (PDDocument source = Loader.loadPDF(pdfBytes);
                 PDDocument singlePage = new PDDocument()) {
                singlePage.importPage(source.getPage(i));
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                singlePage.save(output);
                System.out.println("Page " + i + " size: " + output.toByteArray().length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
