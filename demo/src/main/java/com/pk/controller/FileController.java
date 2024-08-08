package com.pk.controller;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.*;

@RestController
@RequestMapping("/api")
class FileController {

    @PostMapping("/convert")
    public ResponseEntity<List<Map<String, String>>> convertPdfToExcel(@RequestParam("file") MultipartFile file) {
        try {
            // Step 1: Convert PDF to text
            PDDocument document = PDDocument.load(file.getInputStream());
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            document.close();

            // Step 2: Create Excel file
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Transactions");

            // Assuming the text contains transaction data in a structured format
            String[] lines = text.split("\n");
            int rowNum = 0;

            for (String line : lines) {
                Row row = sheet.createRow(rowNum++);
                String[] columns = line.split("\t"); // Assuming tab-separated values
                for (int colNum = 0; colNum < columns.length; colNum++) {
                    row.createCell(colNum).setCellValue(columns[colNum]);
                }
            }

            // Step 3: Parse Excel file for transaction data
            List<Map<String, String>> transactions = parseExcelFile(workbook);

            // Clean up resources
            workbook.close();

            return new ResponseEntity<>(transactions, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<Map<String, String>> parseExcelFile(Workbook workbook) {
        List<Map<String, String>> transactions = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = sheet.iterator();
        Row headerRow = iterator.next();

        while (iterator.hasNext()) {
            Row currentRow = iterator.next();
            Map<String, String> transaction = new HashMap<>();
            for (int i = 0; i < currentRow.getPhysicalNumberOfCells(); i++) {
                transaction.put(headerRow.getCell(i).getStringCellValue(), currentRow.getCell(i).getStringCellValue());
            }
            transactions.add(transaction);
        }

        return transactions;
    }
}
