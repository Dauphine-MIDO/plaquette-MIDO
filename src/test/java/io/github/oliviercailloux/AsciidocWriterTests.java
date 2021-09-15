package io.github.oliviercailloux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import io.github.oliviercailloux.xml_utils.DocBookUtils;
import io.github.oliviercailloux.xml_utils.XmlUtils;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

class AsciidocWriterTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(AsciidocWriterTests.class);

  private String getSingleParagraph(String xml) {
    final Document adocXmlDoc = XmlUtils.asDocument(new InputSource(new StringReader(xml)));
    final Element root = adocXmlDoc.getDocumentElement();
    assertEquals("simpara", root.getTagName());
    final NodeList childNodes = root.getChildNodes();
    assertEquals(1, childNodes.getLength());
    final Node childNode = childNodes.item(0);
    assertEquals(Node.TEXT_NODE, childNode.getNodeType());
    final String textContent = childNode.getNodeValue();
    return textContent;
  }

  @Test
  void testComplex() {
    final AsciidocWriter writer = new AsciidocWriter();
    String complex =
        "a *starred* version and an _underlined_, ‘quoted’, a http://url.com url ’’ two-quotes, with `back-quoted * star` and also `+` plus ``++` double-plus";
    writer.verbatim(complex);
    final String written = writer.toString();

    final Asciidoctor adocConverter = Asciidoctor.Factory.create();
    final String adocXml =
        adocConverter.convert(written, OptionsBuilder.options().backend("docbook").get());
    LOGGER.debug("Xml: {}.", adocXml);
    final String textContent = getSingleParagraph(adocXml);
    assertEquals(complex, textContent);

    writer.verbatim("a *starred* on\ntwo *`lines`*!");
  }

  @Test
  void testTwoLines() {
    final AsciidocWriter writer = new AsciidocWriter();
    final String content = "a *starred* on\ntwo *`lines`*!";
    writer.verbatim(content);
    final String written = writer.toString();

    final Asciidoctor adocConverter = Asciidoctor.Factory.create();
    final String adocXml =
        adocConverter.convert(written, OptionsBuilder.options().backend("docbook").get());
    LOGGER.debug("Xml: {}.", adocXml);
    final String textContent = getSingleParagraph(adocXml);
    assertEquals(content, textContent);

  }

  @Test
  void testValid() throws Exception {
    final AsciidocWriter writer = new AsciidocWriter();
    final String content = "a *starred* on\ntwo *`lines`*!";
    writer.verbatim(content);
    final String written = writer.toString();

    final Asciidoctor adocConverter = Asciidoctor.Factory.create();
    {
      final String docBookPartial =
          adocConverter.convert(written, OptionsBuilder.options().backend("docbook").get());
      final boolean partialValid =
          DocBookUtils.validate(new StreamSource(new StringReader(docBookPartial)));
      assertFalse(partialValid);
    }
    {
      final String docBookFull = adocConverter.convert(written,
          OptionsBuilder.options().headerFooter(true).backend("docbook").get());
      final boolean valid = DocBookUtils.validate(new StreamSource(new StringReader(docBookFull)));
      assertTrue(valid);
    }
  }

  // @Test
  void testBasicTransform() throws Exception {
    TransformerFactory.newDefaultInstance().newTransformer(new StreamSource(
        new File("/usr/share/xml/docbook/stylesheet/docbook-xsl-ns/fo/docbook.xsl")));
  }

  @Test
  void testTransform() throws Exception {
    final AsciidocWriter writer = new AsciidocWriter();
    final String content = "a *starred* on\ntwo *`lines`*!";
    writer.verbatim(content);
    final String written = writer.toString();

    final Asciidoctor adocConverter = Asciidoctor.Factory.create();
    final String docBookFull = adocConverter.convert(written,
        OptionsBuilder.options().headerFooter(true).backend("docbook").get());
    final StreamSource docBookInput = new StreamSource(new StringReader(docBookFull));
    final String transformed = DocBookUtils.asFop(docBookInput);
    LOGGER.debug("Transformed: {}.", transformed);
  }

  @Test
  void testTable() throws Exception {
    final AsciidocWriter writer = new AsciidocWriter();
    final ImmutableList<String> r1 = ImmutableList.of("c1", "c2");
    final ImmutableList<String> r2 = ImmutableList.of();
    final ImmutableList<String> r3 = ImmutableList.of("c1 last");
    writer.table("1, 1", ImmutableList.of("h1", "h2"), ImmutableList.of(r1, r2, r3));

    final String expected =
        Files.readString(Path.of(AsciidocWriterTests.class.getResource("Table.adoc").toURI()));
    assertEquals(expected, writer.toString());
  }
}
