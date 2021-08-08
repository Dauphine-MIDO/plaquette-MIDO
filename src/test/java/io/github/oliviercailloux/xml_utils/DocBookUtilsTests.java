package io.github.oliviercailloux.xml_utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

class DocBookUtilsTests {

  @Test
  void testPdf() throws Exception {
    try (ByteArrayOutputStream pdfStream = new ByteArrayOutputStream()) {
      final Source src =
          new StreamSource(DocBookUtilsTests.class.getResourceAsStream("article.fo"));
      DocBookUtils.asPdf(src, pdfStream);
      final byte[] pdf = pdfStream.toByteArray();
      assertTrue(pdf.length >= 10);
      try (PDDocument document = PDDocument.load(pdf)) {
        final int numberOfPages = document.getNumberOfPages();
        assertEquals(1, numberOfPages);
        assertEquals("My Article", document.getDocumentInformation().getTitle());
      }
    }
  }

  @Test
  void testPdfFailure() throws Exception {
    try (ByteArrayOutputStream pdfStream = new ByteArrayOutputStream()) {
      final Source src =
          new StreamSource(DocBookUtilsTests.class.getResourceAsStream("wrong-fop.fo"));
      assertThrows(RuntimeException.class, () -> DocBookUtils.asPdf(src, pdfStream));
    }
  }
}
