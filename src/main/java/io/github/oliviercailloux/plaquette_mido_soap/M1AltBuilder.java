package io.github.oliviercailloux.plaquette_mido_soap;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.MoreFiles;
import ebx.ebx_dataservices.StandardException;
import io.github.oliviercailloux.publish.AsciidocWriter;
import io.github.oliviercailloux.publish.DocBookConformityChecker;
import io.github.oliviercailloux.publish.DocBookTransformer;
import io.github.oliviercailloux.publish.ToBytesTransformer;
import jakarta.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.transform.stream.StreamSource;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schemas.ebx.dataservices_1.CourseType.Root.Course;
import schemas.ebx.dataservices_1.PersonType.Root.Person;
import schemas.ebx.dataservices_1.ProgramType.Root.Program;

public class M1AltBuilder {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(M1AltBuilder.class);

  private static final boolean WRITE_HTML = false;

  public static final String MENTION_ID = "FRUAI0750736TPRMEA5IFO";
  /**
   * Wendy
   */
  public static final String MAIN_MANAGER_PERSON_ID = "FRUAI0750736TPEIN1122";

  /**
   * Frédéric
   */
  public static final String MAIN_MANAGER_2_PERSON_ID = "FRUAI0750736TPEIN711";

  /**
   * Ouissem SAFRAOU
   */
  public static final String MAIN_MANAGER_3_PERSON_ID = "FRUAI0750736TPEIN14902";

  public static final String PROGRAM_IDENT = "PRA4AMIA-100";

  public static final String PROGRAM_ID_PREFIX = "FRUAI0750736TPR";

  public static final String PROGRAM_ID = PROGRAM_ID_PREFIX + PROGRAM_IDENT;

  public static final String PROGRAM_NAME = "MIAGE" + " - " + "1re année de Master";
  public static final String PROGRAM_URL =
      "https://dauphine.psl.eu/formations/masters/informatique/1re-annee-de-master-miage/programme";
  public static final String PROGRAM_ID_S1 = "FRUAI0750736TPRCPA4AMIA-100-S1";
  public static final String PROGRAM_ID_S1_L1 = "FRUAI0750736TPRCPA4AMIA-100-S1L1";
  public static final String S1_L1_NAME = "Tronc commun";

  public static final String PROGRAM_ID_S1_L2 = "FRUAI0750736TPRCPA4AMIAS1L2";

  public static final String S1_L2_NAME = "Bloc UE d'application";

  public static final String PROGRAM_ID_S2 = "FRUAI0750736TPRCPA4AMIA-100-S2";

  public static final String PROGRAM_ID_S2_L1 = "FRUAI0750736TPRCPA4AMIA-100-S2L1";

  public static final String S2_L1_NAME = "Tronc commun";

  public static final String PROGRAM_ID_S2_L2 = "FRUAI0750736TPRCPA4AMIA-100-S2L2";

  public static final String S2_L2_NAME = "Options";

  public static void main(String[] args) throws Exception {
    LOGGER.info("Obtained {}.", M1AltBuilder.class.getResource("M1ApprBuilder.class"));

    AuthenticatorHelper.setDefaultAuthenticator();

    final M1AltBuilder builder = new M1AltBuilder();
    builder.proceed();
  }

  private final AsciidocWriter writer;

  private Cacher cache;

  private final Querier querier;

  public M1AltBuilder() {
    writer = new AsciidocWriter();
    cache = null;
    querier = Querier.instance();
  }

  private void proceed() throws StandardException, IOException {
    final ImmutableSet<String> programs = ImmutableSet.of(PROGRAM_ID, PROGRAM_ID_S1,
        PROGRAM_ID_S1_L1, PROGRAM_ID_S2, PROGRAM_ID_S2_L1, PROGRAM_ID_S2_L2);
    cache = Cacher.cache(querier, programs);

    verify();

    writer.h1("Programme des cours du M1 MIAGE en alternance");
    writer.addAttribute("lang", "fr");
    writer.addAttribute("toc", "preamble");
    writer.eol();
    writer.paragraph("Généré le "
        + DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.FRANCE)
            .withZone(ZoneId.of("Europe/Paris")).format(Instant.now())
        + " à partir des données du " + PROGRAM_URL + "[site internet] de Dauphine.");

    writeSummary();

    {
      final String subProgramName =
          cache.getProgram(PROGRAM_ID_S1).getProgramName().getValue().getFr().getValue();
      writer.h2(subProgramName);
    }

    {
      final Program program = cache.getProgram(PROGRAM_ID_S1_L1);
      final String programNameFr = program.getProgramName().getValue().getFr().getValue();
      writer.h3(programNameFr);

      for (Course course : cache.getProgramCourses(PROGRAM_ID_S1_L1).values()) {
        writeCourse(course);
      }
    }

    // {
    // final Program program = cache.getProgram(PROGRAM_ID_S1_L2);
    // Verify.verify(program.getProgramStructure().getValue().getRefProgram().isEmpty());
    // final String programNameFr = program.getProgramName().getValue().getFr().getValue();
    // Verify.verify(programNameFr.equals(S1_L2_NAME), programNameFr);
    // writer.h3(programNameFr);
    //
    // for (Course course : cache.getProgramCourses(PROGRAM_ID_S1_L2).values()) {
    // writeCourse(course);
    // }
    // }

    {
      final String subProgramName =
          cache.getProgram(PROGRAM_ID_S2).getProgramName().getValue().getFr().getValue();
      writer.h2(subProgramName);
    }

    {
      final Program program = cache.getProgram(PROGRAM_ID_S2_L1);
      final String programNameFr = program.getProgramName().getValue().getFr().getValue();
      writer.h3(programNameFr);

      for (Course course : cache.getProgramCourses(PROGRAM_ID_S2_L1).values()) {
        writeCourse(course);
      }
    }

    {
      final Program program = cache.getProgram(PROGRAM_ID_S2_L2);
      final String programNameFr = program.getProgramName().getValue().getFr().getValue();
      writer.h3(programNameFr);

      for (Course course : cache.getProgramCourses(PROGRAM_ID_S2_L2).values()) {
        writeCourse(course);
      }
    }

    final String adoc = writer.getContent();
    Files.writeString(Paths.get("out.adoc"), adoc);

    LOGGER.info("Creating Asciidoctor converter.");
    final String docBook;
    try (Asciidoctor adocConverter = Asciidoctor.Factory.create()) {
      LOGGER.info("Converting to Docbook.");
      docBook = adocConverter.convert(adoc,
          Options.builder().headerFooter(true).backend("docbook").build());
    }
    LOGGER.info("Validating Docbook.");
    LOGGER.debug("Docbook: {}.", docBook);
    DocBookConformityChecker.usingDefaults()
        .verifyValid(new StreamSource(new StringReader(docBook)));
    final StreamSource myStyle =
        new StreamSource(M1AltBuilder.class.getResource("dauphine.xsl").toString());
    final ToBytesTransformer toPdf =
        DocBookTransformer.usingDefaultFactory().usingFoStylesheet(myStyle, ImmutableMap.of())
            .asDocBookToPdfTransformer(Path.of("non-existent-" + Instant.now()).toUri());
    toPdf.toSink(new StreamSource(new StringReader(docBook)),
        MoreFiles.asByteSink(Path.of("out.pdf")));
  }

  private void writeSummary() {
    writer.h2("Vue d’ensemble");
    final ImmutableList.Builder<ImmutableList<String>> summaryBuilder = ImmutableList.builder();
    for (Course course : cache.getCourses().values()) {
      final String courseId = course.getCourseID();
      final ImmutableSet<Person> teachers = cache.getCourseTeachers(courseId).values();
      final String names = teachers.stream()
          .map(t -> t.getGivenName().getValue() + " " + t.getFamilyName().getValue())
          .collect(Collectors.joining("; "));
      final ImmutableList<String> row = ImmutableList.of(
          course.getCourseName().getValue().getFr().getValue(), names, course.getEcts().getValue());
      summaryBuilder.add(row);
    }
    final boolean someMultipleTeachers = cache.getCourses().keySet().stream()
        .map(cache::getCourseTeachers).map(Map::values).map(Collection::size).anyMatch(i -> i >= 2);
    final String resp =
        someMultipleTeachers ? "Enseignants responsables" : "Enseignant responsable";
    writer.table("6, 6, 1", ImmutableList.of("Cours", resp, "ECTS"), summaryBuilder.build());
  }

  private void verify() {
    final Program main = cache.getProgram(PROGRAM_ID);
    Verify.verify(main.getIdent().getValue().equals(PROGRAM_IDENT));
    Verify.verify(main.getProgramID().equals(PROGRAM_ID_PREFIX + PROGRAM_IDENT));
    {
      final String programNameFr = main.getProgramName().getValue().getFr().getValue();
      Verify.verify(programNameFr.equals(PROGRAM_NAME), programNameFr);
    }
    Verify.verify(main.getRefMention().getValue().equals(MENTION_ID));
    final List<String> subPrograms = main.getProgramStructure().getValue().getRefProgram();
    Verify.verify(subPrograms.equals(ImmutableList.of(PROGRAM_ID_S1, PROGRAM_ID_S2)));

    final Program s1 = cache.getProgram(PROGRAM_ID_S1);
    Verify.verify(s1.getRefMention().getValue().equals(MENTION_ID));
    final List<String> refProgram = s1.getProgramStructure().getValue().getRefProgram();
    Verify.verify(refProgram.equals(ImmutableList.of(PROGRAM_ID_S1_L1)), refProgram.toString());
    final Program s2 = cache.getProgram(PROGRAM_ID_S2);
    Verify.verify(s2.getRefMention().getValue().equals(MENTION_ID));
    final List<String> refProgramS2 = s2.getProgramStructure().getValue().getRefProgram();
    Verify.verify(refProgramS2.equals(ImmutableList.of(PROGRAM_ID_S2_L1, PROGRAM_ID_S2_L2)),
        ImmutableList.copyOf(refProgramS2).toString());

    final Program s1l1 = cache.getProgram(PROGRAM_ID_S1_L1);
    Verify.verify(s1l1.getProgramStructure().getValue().getRefProgram().isEmpty());
    {
      final String programNameFr = s1l1.getProgramName().getValue().getFr().getValue();
      Verify.verify(programNameFr.equals(S1_L1_NAME), programNameFr);
    }

    final Program s2l1 = cache.getProgram(PROGRAM_ID_S2_L1);
    Verify.verify(s2l1.getProgramStructure().getValue().getRefProgram().isEmpty());
    {
      final String programNameFr = s2l1.getProgramName().getValue().getFr().getValue();
      Verify.verify(programNameFr.equals(S2_L1_NAME), programNameFr);
    }

    final Program s2l2 = cache.getProgram(PROGRAM_ID_S2_L2);
    Verify.verify(s2l2.getProgramStructure().getValue().getRefProgram().isEmpty());
    {
      final String programNameFr = s2l2.getProgramName().getValue().getFr().getValue();
      Verify.verify(programNameFr.equals(S2_L2_NAME), programNameFr);
    }
  }

  private void writeCourse(Course course) {
    final String courseName = course.getCourseName().getValue().getFr().getValue();
    writer.h4(courseName);
    final String volume = course.getVolume().getValue();
    Verify.verify(volume.equals("0") == courseName.equals("Mémoire"), courseName);
    final String volumeText = volume.equals("0") ? "" : volume + " h" + " ; ";
    writer.paragraph(volumeText + course.getEcts().getValue() + " ECTS");
    
    Verify.verify(course.getAdmissionInfo() == null);
    // Verify.verify(course.getCoefficient().getValue().getFr().getValue()
    // .equals("\n<p>Capitalisation : Non</p>\n<br/>"));
    // LOGGER.info(course.getCoefficient().getValue().getFr().getValue());
    final ImmutableSet<Person> teachers = cache.getCourseTeachers(course.getCourseID()).values();
    // Verify.verify(teachers.isEmpty() == courseName.equals("Mémoire"), courseName);
    if (!teachers.isEmpty()) {
      final String names = teachers.stream()
          .map(t -> t.getGivenName().getValue() + " " + t.getFamilyName().getValue())
          .collect(Collectors.joining("; "));
      final String prefix =
          teachers.size() == 1 ? "Enseignant responsable : " : "Enseignants responsables : ";
      writer.paragraph(prefix + names);
    }
    Verify.verify(course.getCourseIntroduction() == null);
    Verify.verify(course.getFormOfTeaching() == null);
    Verify.verify(course.getLevel() == null);
    Verify.verify(course.getLevelLang() == null);
    Verify.verify(
        ImmutableSet.of(MAIN_MANAGER_PERSON_ID, MAIN_MANAGER_2_PERSON_ID, MAIN_MANAGER_3_PERSON_ID)
            .contains(course.getManagingTeacher().getValue()),
        valueOpt(course.getManagingTeacher()).toString());
    Verify.verify(course.getTeachingLang().equals(ImmutableList.of("fr"))
        || course.getTeachingLang().equals(ImmutableList.of("fr+en")));
    Verify.verify(course.getTeachers().isEmpty());
    writer.eol();
    final Optional<String> recommendedPrerequisitesOpt =
        valueOpt(course.getRecommendedPrerequisites(), Course.RecommendedPrerequisites::getFr);
    addOptionalSection("Prérequis recommandés", recommendedPrerequisitesOpt);
    final Optional<String> formalPrerequisitesOpt =
        valueOpt(course.getFormalPrerequisites(), Course.FormalPrerequisites::getFr);
    addOptionalSection("Prérequis obligatoires", formalPrerequisitesOpt);
    final Optional<String> learningObjectivesOpt =
        valueOpt(course.getLearningObjectives(), Course.LearningObjectives::getFr);
    addOptionalSection("Compétences à acquérir", learningObjectivesOpt);
    final Optional<String> courseDescriptionOpt =
        valueOpt(course.getCourseDescription(), Course.CourseDescription::getFr);
    addOptionalSection("Contenu", courseDescriptionOpt);
    if (courseDescriptionOpt.isEmpty()) {
      Verify.verify(courseName.equals("Mémoire"), courseName);
    }
    final Optional<String> syllabusOpt = valueOpt(course.getSyllabus(), Course.Syllabus::getFr);
    addOptionalSection("Références", syllabusOpt);
    final Optional<String> formOfAssessmentOpt =
        valueOpt(course.getFormOfAssessment(), Course.FormOfAssessment::getFr);
    addOptionalSection("Évaluation", formOfAssessmentOpt);
  }

  private void addOptionalSection(final String title, final Optional<String> contentOpt) {
    if (contentOpt.isPresent()) {
      final String content = contentOpt.get();
      if (WRITE_HTML) {
        writer.h5(title + " html");
        writer.verbatim(content);
        writer.eol();
      }
      writer.h5(title);
      writer.append(getText(content));
      writer.eol();
    }
  }

  private String getText(String htmlText) {
    final Document parsed = Jsoup.parse(htmlText);
    final List<Node> children = parsed.body().childNodes();
    return getText(children);
  }

  private String getText(List<Node> children) {
    final StringBuilder textBuilder = new StringBuilder();
    for (Node node : children) {
      final String text = getText(node);
      textBuilder.append(text);
    }
    final String text = textBuilder.toString();
    return text;
  }

  private String getText(Node node) {
    final String text;
    if (node instanceof TextNode) {
      text = AsciidocWriter.quote(((TextNode) node).text().strip());
    } else if (node instanceof Element) {
      final Element element = (Element) node;
      final String tag = element.normalName();
      if (tag.equals("br")) {
        text = "\n\n";
      } else if (tag.equals("strong")) {
        text = "*" + getText(element.childNodes()) + "*";
      } else if (tag.equals("p")) {
        text = "\n\n" + getText(element.childNodes());
      } else if (tag.equals("ul")) {
        text = "\n\n" + getText(element.childNodes());
      } else if (tag.equals("li")) {
        final List<Node> liChildren = element.childNodes();
        Verify.verify(liChildren.size() == 1);
        final Node liChild = liChildren.get(0);
        Verify.verify(liChild instanceof TextNode);
        final String inner = ((TextNode) liChild).text();
        Verify.verify(!inner.isBlank());
        text = "- " + inner + "\n";
      } else {
        throw new IllegalArgumentException(node.outerHtml());
      }
    } else {
      throw new IllegalArgumentException(node.outerHtml());
    }
    return text;
  }

  public static <T> Optional<T> valueOpt(JAXBElement<T> element) {
    return element == null ? Optional.empty() : Optional.of(element.getValue());
  }

  public <F, T> Optional<T> valueOpt(JAXBElement<F> element,
      Function<F, JAXBElement<T>> toFunction) {
    return element == null ? Optional.empty()
        : Optional.of(element.getValue()).map(toFunction).map(JAXBElement::getValue);
  }
}
