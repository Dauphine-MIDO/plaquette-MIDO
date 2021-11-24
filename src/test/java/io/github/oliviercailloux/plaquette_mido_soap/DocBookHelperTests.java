package io.github.oliviercailloux.plaquette_mido_soap;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.oliviercailloux.publish.DocBookHelper;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.time.Instant;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These tests are mostly redundant with those in Publish; but serve to check whether the helper
 * still works when its data is packaged in a jar.
 */
class DocBookHelperTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DocBookHelperTests.class);

  /**
   * Attempting to convert "docbook howto shortened.xml" to PDF fails. This seems to be too complex
   * for this process. Tables are not supported; and even without tables, it complains about some
   * line overflow (including without my custom styling). I didn’t investigate further.
   */
  @Test
  void testDocBookSimpleArticleToPdf() throws Exception {
    final StreamSource docBook = new StreamSource(
        DocBookHelperTests.class.getResource("docbook simple article.xml").toString());

    final DocBookHelper helper = DocBookHelper.instance();
    helper.verifyValid(docBook);
    final StreamSource myStyle =
        new StreamSource(DocBookHelper.class.getResource("mystyle.xsl").toString());
    try (ByteArrayOutputStream pdfStream = new ByteArrayOutputStream()) {
      helper.docBookToPdf(Path.of("non-existent-" + Instant.now()).toUri(), docBook, myStyle,
          pdfStream);
      final byte[] pdf = pdfStream.toByteArray();
      assertTrue(pdf.length >= 10);
    }
  }
}
