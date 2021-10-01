package io.github.oliviercailloux.xml_utils;

import com.google.common.base.Verify;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.ResolverFactory;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.xml.sax.CountingErrorHandler;
import com.thaiopensource.xml.sax.DraconianErrorHandler;
import io.github.oliviercailloux.jaris.xml.XmlUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.apache.fop.ResourceEventProducer;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.events.LoggingEventListener;
import org.apache.fop.events.model.EventSeverity;
import org.apache.fop.hyphenation.Hyphenator;
import org.apache.xmlgraphics.util.MimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * The public API of this class favors {@link StreamSource} (from {@code javax.xml.transform}) to
 * {@link InputSource} (from {@code org.xml.sax}). Both classes come from the {@code java.xml}
 * module, and their APIs are almost identical, the only difference being that {@code InputSource}
 * has an “encoding” parameter; and that {@code StreamSource} is part of a hierarchy (as it
 * implements {@link Source}), which makes it nicer to use in this context. See also
 * <a href="https://stackoverflow.com/q/69194590">SO</a>.
 */
public class DocBookUtils {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DocBookUtils.class);

  public static void validate(StreamSource docBook) throws SAXException, IOException {
    final StreamSource schemaSource =
        new StreamSource(DocBookUtils.class.getResource("docbook.rng").toString());
//    final io.github.oliviercailloux.jaris.xml.XmlUtils.Validator validator = XmlUtils.validator();
//    validator.setSchema(schemaSource);
//    validator.validate(docBook);
    return validate(docBook, schemaSource);
  }

  public static boolean validate(StreamSource document, StreamSource relaxSchema) {
    return validate(XmlUtils.toInputSource(document), XmlUtils.toInputSource(relaxSchema));
  }

  private static boolean validate(InputSource document, InputSource relaxSchema) {
    try {
      final CountingErrorHandler countingErrorHandler = new CountingErrorHandler();
      final ContentHandler contentHandler;
      final XMLReader xmlReader;
      {
        final PropertyMap countingErrorProperties;
        final PropertyMapBuilder propBuilder = new PropertyMapBuilder(PropertyMap.EMPTY);
        propBuilder.put(ValidateProperty.ERROR_HANDLER, countingErrorHandler);
        countingErrorProperties = propBuilder.toPropertyMap();
        final com.thaiopensource.validate.Schema schema =
            new AutoSchemaReader().createSchema(relaxSchema, countingErrorProperties);
        final Validator validator = schema.createValidator(countingErrorProperties);
        contentHandler = validator.getContentHandler();
        xmlReader = ResolverFactory.createResolver(PropertyMap.EMPTY).createXMLReader();
        // xmlReader.setp
        // new Sax2XMLReaderCreator().
      }
      {
        xmlReader.setErrorHandler(countingErrorHandler);
        xmlReader.setContentHandler(contentHandler);
        xmlReader.parse(document);
        return (countingErrorHandler.getFatalErrorCount() == 0)
            && (countingErrorHandler.getErrorCount() == 0)
            && (countingErrorHandler.getWarningCount() == 0);
      }
    } catch (SAXException | IOException e) {
      throw new IllegalStateException(e);
    } catch (IncorrectSchemaException e) {
      throw new IllegalStateException(e);
    }
  }

  public static void validate(String documentId, InputStream relaxSchema)
      throws SAXException, ParserConfigurationException, IOException {
    ErrorHandler errorHandler = new DraconianErrorHandler();

    System.setProperty(SchemaFactory.class.getName() + ":" + XMLConstants.RELAXNG_NS_URI,
        "com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory");
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI);
    schemaFactory.setErrorHandler(errorHandler);
    Schema schema = schemaFactory.newSchema(new StreamSource(relaxSchema));

    final Source document = new SAXSource();
    document.setSystemId(documentId);
    schema.newValidator().validate(document);
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setSchema(schema);

    SAXParser parser = factory.newSAXParser();
    XMLReader reader = parser.getXMLReader();
    reader.setContentHandler(schema.newValidator());
    reader.setErrorHandler(errorHandler);
    reader.parse(documentId);
  }

  static boolean validate(InputSource docBook) {
    InputSource schemaSource =
        new InputSource(DocBookUtils.class.getResource("docbook.rng").toString());
    return validate(docBook, schemaSource);
  }

  static String asFop(InputSource docBook) throws IOException {
    final DOMSource source = new DOMSource(XmlUtils.asDocument(docBook));
    return asFop(source);
  }

  public static String asFop(Source docBook) throws IOException {
    try (InputStream myStyle = DocBookUtils.class.getResourceAsStream("mystyle.xsl")) {
      return XmlUtils.transform(docBook, new StreamSource(myStyle));
    }
  }

  public static void asPdf(Source fo, OutputStream pdfStream) {
    final URL configUrl = DocBookUtils.class.getResource("fop-config.xml");
    try (InputStream configStream = configUrl.openStream()) {
      final FopFactory fopFactory;
      try {
        fopFactory =
            FopFactory.newInstance(Hyphenator.class.getResource(".").toURI(), configStream);
      } catch (URISyntaxException | SAXException e) {
        throw new IllegalStateException(e);
      }
      final FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
      foUserAgent.getEventBroadcaster().addEventListener(new LoggingEventListener());
      foUserAgent.getEventBroadcaster().addEventListener((e) -> {
        /* https://xmlgraphics.apache.org/fop/2.4/events.html */
        if (ResourceEventProducer.class.getName().equals(e.getEventGroupID())) {
          e.setSeverity(EventSeverity.FATAL);
        } else {
          // ignore all other events (or do something of your choice)
        }
      });

      final Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfStream);
      Verify.verify(fopFactory.validateStrictly());
      Verify.verify(fopFactory.validateUserConfigStrictly());
      final TransformerFactory factory = TransformerFactory.newInstance();
      final Transformer transformer = factory.newTransformer();
      final Result res = new SAXResult(fop.getDefaultHandler());
      transformer.transform(fo, res);
    } catch (IOException | FOPException | TransformerException e) {
      throw new IllegalStateException(e);
    }
  }
}
