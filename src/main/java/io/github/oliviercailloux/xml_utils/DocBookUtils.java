package io.github.oliviercailloux.xml_utils;

import com.google.common.base.Verify;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.ResolverFactory;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.xml.sax.CountingErrorHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * The public API of this class favors {@link StreamSource} (from {@code javax.xml.transform}) to
 * {@link InputSource} (from {@code org.xml.sax}). Both classes come from the {@code java.xml}
 * module, and their APIs are almost identical, the only difference being that {@code InputSource}
 * has an “encoding” parameter; and that {@code StreamSource} is part of a hierarchy (as it
 * implements {@link Source}), which makes it nicer to use in this context.
 */
public class DocBookUtils {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DocBookUtils.class);

  @SuppressWarnings("resource")
  private static InputSource toInputSource(StreamSource document) {
    final InputSource inputSource = new InputSource();

    {
      final InputStream inputStream = document.getInputStream();
      if (inputStream != null) {
        inputSource.setByteStream(inputStream);
      }
    }
    {
      final Reader reader = document.getReader();
      if (reader != null) {
        inputSource.setCharacterStream(reader);
      }
    }
    {
      final String publicId = document.getPublicId();
      if (publicId != null) {
        inputSource.setPublicId(publicId);
      }
    }
    {
      final String systemId = document.getSystemId();
      if (systemId != null) {
        inputSource.setSystemId(systemId);
      }
    }
    return inputSource;
  }

  public static boolean validate(StreamSource docBook) {
    final StreamSource schemaSource =
        new StreamSource(DocBookUtils.class.getResource("docbook.rng").toString());
    return validate(docBook, schemaSource);
  }

  public static boolean validate(StreamSource document, StreamSource relaxSchema) {
    return validate(toInputSource(document), toInputSource(relaxSchema));
  }

  private static boolean validate(InputSource document, InputSource relaxSchema) {
    try {
      final Schema schema;
      final CountingErrorHandler countingErrorHandler = new CountingErrorHandler();
      {
        final AutoSchemaReader reader = new AutoSchemaReader();
        final PropertyMapBuilder propBuilder = new PropertyMapBuilder(PropertyMap.EMPTY);
        propBuilder.put(ValidateProperty.ERROR_HANDLER, countingErrorHandler);
        final PropertyMap schemaProperties = propBuilder.toPropertyMap();
        schema = reader.createSchema(relaxSchema, schemaProperties);
      }
      {
        final PropertyMapBuilder propBuilder = new PropertyMapBuilder(PropertyMap.EMPTY);
        propBuilder.put(ValidateProperty.ERROR_HANDLER, countingErrorHandler);
        final PropertyMap instanceProperties = propBuilder.toPropertyMap();
        final Validator validator = schema.createValidator(instanceProperties);
        final XMLReader xmlReader =
            ResolverFactory.createResolver(instanceProperties).createXMLReader();
        xmlReader.setErrorHandler(countingErrorHandler);
        xmlReader.setContentHandler(validator.getContentHandler());
        xmlReader.parse(document);
        return (countingErrorHandler.getFatalErrorCount() == 0)
            && (countingErrorHandler.getErrorCount() == 0)
            && (countingErrorHandler.getWarningCount() == 0);
      }
    } catch (SAXException | IOException | IncorrectSchemaException e) {
      throw new IllegalStateException(e);
    }
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
