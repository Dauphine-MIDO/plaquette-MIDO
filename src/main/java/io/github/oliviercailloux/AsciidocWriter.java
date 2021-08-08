package io.github.oliviercailloux;

import static com.google.common.base.Preconditions.checkArgument;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;

/**
 * TODO should sanitize input.
 *
 * @author Olivier Cailloux
 *
 */
public class AsciidocWriter {
  public static void main(String[] args) throws Exception {
    final Asciidoctor adocConverter = Asciidoctor.Factory.create();

    final String adoc = "= Title\nBlah\n\nBlih\n";
    Files.writeString(Paths.get("out.adoc"), adoc);
    final String adocHtml = adocConverter.convert(adoc, OptionsBuilder.options().backend("html"));
    Files.writeString(Paths.get("out.html"), adocHtml);
    final String adocXml =
        adocConverter.convert(adoc, OptionsBuilder.options().backend("docbook").get());
    Files.writeString(Paths.get("out.xml"), adocXml);
    final String adocPdf = adocConverter.convert(adoc, OptionsBuilder.options().backend("pdf"));
    Files.writeString(Paths.get("out.pdf"), adocPdf);

  }

  public static String quote(String text) {
    if (text.isEmpty()) {
      return "";
    }

    final String replaced = text.replace("\r\n", "\n");
    checkArgument(!replaced.contains("\r"));
    final String quoted = "+" + replaced.replace("+", "pass:[+]") + "+";
    return quoted;
  }

  private final StringBuilder content;

  public AsciidocWriter() {
    content = new StringBuilder();
  }

  public void title(int level, String title) {
    content.append("=".repeat(level) + " " + title + '\n');
  }

  public void addAttribute(String attribute, String value) {
    content.append(":" + attribute + ":" + " " + value + "\n");
  }

  public void h1(String title) {
    title(1, title);
  }

  public void h2(String title) {
    title(2, title);
  }

  public void h3(String title) {
    title(3, title);
  }

  public void h4(String title) {
    title(4, title);
  }

  public void append(String text) {
    final String replaced = text.replace("\r\n", "\n");
    checkArgument(!replaced.contains("\r"));
    content.append(replaced);
    if (!replaced.endsWith("\n")) {
      eol();
    }
  }

  public void paragraph(String text) {
    final String replaced = text.replace("\r\n", "\n");
    checkArgument(!replaced.contains("\r"));
    content.append(replaced);
    if (!replaced.endsWith("\n")) {
      eol();
    }
    eol();
  }

  public void paragraph(Optional<String> text) {
    if (text.isPresent()) {
      paragraph(text);
    }
  }

  public void list(List<String> items) {
    for (String item : items) {
      content.append("* " + item + '\n');
    }
  }

  public void eol() {
    content.append('\n');
  }

  public void verbatim(String text) {
    final String quoted = quote(text);
    content.append(quoted);
    eol();
  }

  @Override
  public String toString() {
    return content.toString();
  }
}
