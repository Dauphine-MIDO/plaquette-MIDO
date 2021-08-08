package io.github.oliviercailloux.xml_utils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.serialize.MessageWarner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlUtils {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtils.class);
  private static DocumentBuilder builder;

  public static String toXml(JAXBElement<?> element) {
    /** TODO see if can use for example Transform with a Jaxbsource. */
    try {
      final JAXBContext jc = JAXBContext.newInstance(element.getValue().getClass());
      final Marshaller marshaller = jc.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      final StringWriter wr = new StringWriter();
      marshaller.marshal(element, wr);
      return wr.toString();
    } catch (JAXBException exc) {
      throw new IllegalStateException(exc);
    }
  }

  private static void prepareBuilder() {
    if (builder == null) {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      try {
        builder = factory.newDocumentBuilder();
      } catch (ParserConfigurationException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  public static Document asDocument(InputSource input) {
    prepareBuilder();

    final Document doc;
    try {
      doc = builder.parse(input);
    } catch (SAXException | IOException e) {
      throw new IllegalStateException(e);
    }

    final Element docE = doc.getDocumentElement();
    LOGGER.debug("Main tag name: {}.", docE.getTagName());

    return doc;
  }

  public static String transform(Source document, Source stylesheet) {
    final StringWriter result = new StringWriter();
    try {
      /** Fails with default transformer factory. */
      // final Transformer transformer =
      // TransformerFactory.newDefaultInstance().newTransformer(stylesheet));
      final TransformerFactoryImpl factory = new TransformerFactoryImpl();
      final ErrorListener listener = new ErrorListener() {

        @Override
        public void warning(TransformerException exception) throws TransformerException {
          LOGGER.debug("Warning while processing.", exception);
        }

        @Override
        public void fatalError(TransformerException exception) throws TransformerException {
          LOGGER.error("Fatal error while processing.", exception);
        }

        @Override
        public void error(TransformerException exception) throws TransformerException {
          LOGGER.error("While processing.", exception);
        }
      };
      factory.setErrorListener(listener);
      /**
       * https://www.saxonica.com/html/documentation/configuration/config-features.html
       */
      factory.setAttribute(FeatureKeys.MESSAGE_EMITTER_CLASS, MessageWarner.class.getName());
      final Transformer transformer = factory.newTransformer(stylesheet);
      transformer.transform(document, new StreamResult(result));
    } catch (TransformerException e) {
      throw new IllegalStateException(e);
    }
    return result.toString();
  }
}
