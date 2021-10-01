package io.github.oliviercailloux.xml_utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.oliviercailloux.jaris.xml.XmlUtils;
import java.io.InputStream;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.TransformerFactoryImpl;
import org.junit.jupiter.api.Test;

public class XmlUtilsTests {

  @Test
  void testTransformComplex() throws Exception {
    try (InputStream docBook = XmlUtilsTests.class.getResourceAsStream("docbook howto.xml");
        InputStream myStyle = XmlUtilsTests.class.getResourceAsStream("mystyle.xsl")) {
      final TransformerFactoryImpl factory = new TransformerFactoryImpl();
      assertEquals("", XmlUtils.transformer(factory).transform(new StreamSource(docBook),
          new StreamSource(myStyle)));
    }
  }
}
