package com.web.room.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class InvoiceService {

    public byte[] generateInvoicePdf(Map<String, Object> data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Title
            document.add(new Paragraph("RoomsDekho - Payment Receipt")
                    .setBold()
                    .setFontSize(18));

            document.add(new Paragraph("Invoice No: " + data.get("orderId")));
            document.add(new Paragraph("Date: " + data.get("date")));
            document.add(new Paragraph("Customer: " + data.get("email")));
            document.add(new Paragraph("\n"));

            // Table for Plan Details
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
            table.addHeaderCell("Description");
            table.addHeaderCell("Amount");

            table.addCell("Premium Plan: " + data.get("planName"));
            table.addCell("INR " + data.get("amount"));

            document.add(table);

            document.add(new Paragraph("\nValidity: " + data.get("startDate") + " to " + data.get("endDate")));
            document.add(new Paragraph("\nThank you for choosing RoomsDekho Premium services!"));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
}