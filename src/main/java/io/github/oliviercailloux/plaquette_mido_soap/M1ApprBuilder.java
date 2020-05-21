package io.github.oliviercailloux.plaquette_mido_soap;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamSource;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import ebx.ebx_dataservices.StandardException;
import io.github.oliviercailloux.AsciidocWriter;
import io.github.oliviercailloux.xml_utils.DocBookUtils;
import schemas.ebx.dataservices_1.CourseType.Root.Course;
import schemas.ebx.dataservices_1.ProgramType.Root.Program;

public class M1ApprBuilder {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(M1ApprBuilder.class);

	private static final boolean WRITE_HTML = false;

	static public final String MENTION_ID = "FRUAI0750736TPRMEA5IFO";
	/**
	 * Wendy
	 */
	static public final String MAIN_MANAGER_PERSON_ID = "FRUAI0750736TPEIN1122";

	/**
	 * Frédéric
	 */
	static public final String MAIN_MANAGER_2_PERSON_ID = "FRUAI0750736TPEIN711";

	static public final String PROGRAM_IDENT = "PRA4AMIA-100";
	static public final String PROGRAM_NAME = "Méthodes Informatiques Appliquées pour la Gestion des Entreprises - 1ère année de Master";
	static public final String PROGRAM_ID_S1 = "FRUAI0750736TPRCPA4AMIA-100-S1";
	static public final String PROGRAM_ID_S1_L1 = "FRUAI0750736TPRCPA4AMIA-100-S1L1";
	static public final String S1_L1_NAME = "UE Obligatoires";

	static public final String PROGRAM_ID_S1_L2 = "FRUAI0750736TPRCPA4AMIAS1L2";

	static public final String S1_L2_NAME = "Bloc UE d'application";

	static public final String PROGRAM_ID_S2 = "FRUAI0750736TPRCPA4AMIA-100-S2";

	static public final String PROGRAM_ID_S2_L1 = "FRUAI0750736TPRCPA4AMIA-100-S2L1";

	static public final String S2_L1_NAME = "UE Obligatoires";

	static public final String PROGRAM_ID_S2_L2 = "FRUAI0750736TPRCPA4AMIA-100-S2L2";

	static public final String S2_L2_NAME = "UE Options";

	public static void main(String[] args) throws Exception {
		QueriesHelper.setDefaultAuthenticator();

		final M1ApprBuilder builder = new M1ApprBuilder();
		builder.proceed();
	}

	private final Querier querier;

	private final AsciidocWriter writer;

	public M1ApprBuilder() {
		querier = new Querier();
		writer = new AsciidocWriter();
	}

	private void proceed() throws StandardException, IOException {
		verify();

		writer.h1("Programme du M1 MIAGE en apprentissage");
		writer.addAttribute("lang", "fr");
		writer.eol();
		writer.paragraph("Généré le "
				+ DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.FRANCE)
						.withZone(ZoneId.systemDefault()).format(Instant.now())
				+ " à partir des données du site internet de Dauphine.");

		{
			writer.h2("Semestre 1");
			final Program program = querier.getProgram(PROGRAM_ID_S1_L1);
			Verify.verify(program.getProgramStructure().getValue().getRefProgram().isEmpty());
			Verify.verify(program.getProgramName().getValue().equals(S1_L1_NAME));

			final List<String> courseRefs = program.getProgramStructure().getValue().getRefCourse();
			for (String courseRef : courseRefs) {
				final Course course = querier.getCourse(courseRef);
				writeCourse(course);
			}
		}

		{
			writer.h2("Semestre 1, Bloc UE d’application");
			final Program program = querier.getProgram(PROGRAM_ID_S1_L2);
			Verify.verify(program.getProgramStructure().getValue().getRefProgram().isEmpty());
			Verify.verify(program.getProgramName().getValue().equals(S1_L2_NAME), program.getProgramName().getValue());

			final List<String> courseRefs = program.getProgramStructure().getValue().getRefCourse();
			for (String courseRef : courseRefs) {
				final Course course = querier.getCourse(courseRef);
				writeCourse(course);
			}
		}

		{
			writer.h2("Semestre 2, UEs obligatoires");
			final Program program = querier.getProgram(PROGRAM_ID_S2_L1);
			Verify.verify(program.getProgramStructure().getValue().getRefProgram().isEmpty());
			Verify.verify(program.getProgramName().getValue().equals(S2_L1_NAME), program.getProgramName().getValue());

			final List<String> courseRefs = program.getProgramStructure().getValue().getRefCourse();
			for (String courseRef : courseRefs) {
				final Course course = querier.getCourse(courseRef);
				writeCourse(course);
			}
		}

		{
			writer.h2("Semestre 2, UEs optionnelles");
			final Program program = querier.getProgram(PROGRAM_ID_S2_L2);
			Verify.verify(program.getProgramStructure().getValue().getRefProgram().isEmpty());
			Verify.verify(program.getProgramName().getValue().equals(S2_L2_NAME), program.getProgramName().getValue());

			final List<String> courseRefs = program.getProgramStructure().getValue().getRefCourse();
			for (String courseRef : courseRefs) {
				final Course course = querier.getCourse(courseRef);
				writeCourse(course);
			}
		}

		final String adoc = writer.toString();
		Files.writeString(Paths.get("out.adoc"), adoc);

		final Asciidoctor adocConverter = Asciidoctor.Factory.create();
		{
			final String docbook = adocConverter.convert(adoc,
					OptionsBuilder.options().headerFooter(true).backend("docbook").get());
			LOGGER.debug("Docbook: {}.", docbook);
			final boolean valid = DocBookUtils.validate(new InputSource(new StringReader(docbook)));
			Verify.verify(valid);
			final String fop = DocBookUtils.asFop(new InputSource(new StringReader(docbook)));
			try (OutputStream outStream = Files.newOutputStream(Path.of("out.pdf"))) {
				DocBookUtils.asPdf(new StreamSource(new StringReader(fop)), outStream);
			}
		}
	}

	private void verify() throws StandardException {
		final String predicate = "ident = '" + M1ApprBuilder.PROGRAM_IDENT + "'";
		final List<Program> programsMain = querier.getPrograms(predicate);
		final Program main = Iterables.getOnlyElement(programsMain);
		Verify.verify(main.getIdent().getValue().equals(PROGRAM_IDENT));
		Verify.verify(main.getProgramName().getValue().equals(PROGRAM_NAME), main.getProgramName().getValue());
		Verify.verify(main.getRefMention().getValue().equals(MENTION_ID));
		final List<String> subPrograms = main.getProgramStructure().getValue().getRefProgram();
		Verify.verify(subPrograms.equals(ImmutableList.of(PROGRAM_ID_S1, PROGRAM_ID_S2)));

		final Program s1 = querier.getProgram(PROGRAM_ID_S1);
		final List<String> refProgram = s1.getProgramStructure().getValue().getRefProgram();
		Verify.verify(refProgram.equals(ImmutableList.of(PROGRAM_ID_S1_L1, PROGRAM_ID_S1_L2)), refProgram.toString());
		final Program s2 = querier.getProgram(PROGRAM_ID_S2);
		Verify.verify(s2.getProgramStructure().getValue().getRefProgram()
				.equals(ImmutableList.of(PROGRAM_ID_S2_L1, PROGRAM_ID_S2_L2)));
	}

	private void writeCourse(Course course) {
		final String courseName = course.getCourseName().getValue();
		writer.h3(courseName);
		writer.paragraph(course.getEcts().getValue() + " ECTS");
		Verify.verify(course.getAdmissionInfo() == null);
		Verify.verify(course.getCoefficient().getValue().equals("\n<p>Capitalisation : Non</p>\n<br/>"));
		Verify.verify(course.getContacts() == null);
		Verify.verify(course.getCourseIntroduction() == null);
		Verify.verify(course.getFormalPrerequisites() == null);
		Verify.verify(course.getFormOfAssessment() == null);
		Verify.verify(course.getFormOfTeaching() == null);
		Verify.verify(course.getLearningObjectives() == null);
		Verify.verify(course.getLevel() == null);
		Verify.verify(course.getLevelLang() == null);
		Verify.verify(
				course.getManagingTeacher().getValue().equals(MAIN_MANAGER_PERSON_ID)
						|| course.getManagingTeacher().getValue().equals(MAIN_MANAGER_2_PERSON_ID),
				valueOrNull(course.getManagingTeacher()));
		Verify.verify(course.getTeachingLang().equals(ImmutableList.of("fr")));
		Verify.verify(course.getTeachers().isEmpty());
		Verify.verify(course.getRecommendedPrerequisites() == null);
		Verify.verify(course.getVolume().getValue().equals("0"));
		writer.eol();
		final Optional<String> courseDescriptionOpt = valueOpt(course.getCourseDescription());
		if (courseDescriptionOpt.isPresent()) {
			final String courseDescription = courseDescriptionOpt.get();
			if (WRITE_HTML) {
				writer.h4("Description html");
				writer.verbatim(courseDescription);
				writer.eol();
				writer.h4("Description");
			}
			writer.append(getText(courseDescription));
			writer.eol();
		} else {
			Verify.verify(courseName.equals("Mémoire"));
		}
		final Optional<String> syllabusOpt = valueOpt(course.getSyllabus());
		if (syllabusOpt.isPresent()) {
			final String syllabus = syllabusOpt.get();
			if (WRITE_HTML) {
				writer.h4("Références html");
				writer.verbatim(syllabus);
				writer.eol();
			}
			writer.h4("Références");
			writer.append(getText(syllabus));
			writer.eol();
		}
//			writer.h3("Objectifs d’apprentissage");
//			writer.paragraph(course.getLearningObjectives().getValue());
//			writer.h3("Prérequis");
//			writer.paragraph(course.getRecommendedPrerequisites().getValue());
//			writer.h3("Évaluation");
//			writer.paragraph(course.getFormOfAssessment().getValue());
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

	public String valueOrNull(JAXBElement<String> element) {
		return element == null ? null : element.getValue();
	}

	public <T> Optional<T> valueOpt(JAXBElement<T> element) {
		return element == null ? Optional.empty() : Optional.of(element.getValue());
	}

}
