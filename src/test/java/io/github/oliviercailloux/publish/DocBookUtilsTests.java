package io.github.oliviercailloux.publish;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.oliviercailloux.jaris.xml.XmlUtils.XmlException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

class DocBookUtilsTests {

  @Test
  void testValid() throws Exception {
    try (InputStream docBook = DocBookUtilsTests.class.getResourceAsStream("docbook howto.xml")) {
      assertDoesNotThrow(() -> DocBookHelper.instance().verifyValid(new StreamSource(docBook)));
    }
  }

  @Test
  void testInvalid() throws Exception {
    try (InputStream docBook =
        DocBookUtilsTests.class.getResourceAsStream("docbook howto invalid.xml")) {
      assertThrows(XmlException.class,
          () -> DocBookHelper.instance().verifyValid(new StreamSource(docBook)));
    }
  }

  @Test
  void testPdf() throws Exception {
    try (ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        InputStream srcStream = DocBookUtilsTests.class.getResourceAsStream("article.fo")) {
      final Source src = new StreamSource(srcStream);
      DocBookHelper.instance().foToPdf(src, pdfStream);
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
    try (ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        InputStream srcStream = DocBookUtilsTests.class.getResourceAsStream("wrong-fop.fo")) {
      final Source src = new StreamSource(srcStream);
      assertThrows(RuntimeException.class, () -> DocBookHelper.instance().foToPdf(src, pdfStream));
    }
  }
}
