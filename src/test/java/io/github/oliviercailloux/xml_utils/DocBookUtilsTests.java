package io.github.oliviercailloux.xml_utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

class DocBookUtilsTests {

  @Test
  void testValid() throws Exception {
    try (InputStream docBook = DocBookUtilsTests.class.getResourceAsStream("docbook howto.xml")) {
      assertTrue(DocBookUtils.validate(new StreamSource(docBook)));
    }
  }

  @Test
  void testValidSax() throws Exception {
    try (InputStream rng = DocBookUtils.class.getResource("docbook.rng").openStream()) {
      assertDoesNotThrow(() -> DocBookUtils
          .validate(DocBookUtilsTests.class.getResource("docbook howto.xml").toString(), rng));
      assertThrows(SAXException.class, () -> DocBookUtils.validate(
          DocBookUtilsTests.class.getResource("docbook howto invalid.xml").toString(), rng));
    }
  }

  @Test
  void testInvalid() throws Exception {
    try (InputStream docBook =
        DocBookUtilsTests.class.getResourceAsStream("docbook howto invalid.xml")) {
      assertFalse(DocBookUtils.validate(new StreamSource(docBook)));
    }
  }

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
