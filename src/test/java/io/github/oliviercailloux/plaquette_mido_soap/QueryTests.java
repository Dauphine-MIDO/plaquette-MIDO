package io.github.oliviercailloux.plaquette_mido_soap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import io.github.oliviercailloux.plaquette.AuthenticatorHelper;
import io.github.oliviercailloux.plaquette.M1AltBuilder;
import io.github.oliviercailloux.plaquette.Querier;
import jakarta.xml.bind.JAXBElement;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schemas.ebx.dataservices_1.CourseType.Root.Course;
import schemas.ebx.dataservices_1.CourseType.Root.Course.Contacts;
import schemas.ebx.dataservices_1.MentionType.Root.Mention;
import schemas.ebx.dataservices_1.PersonType.Root.Person;
import schemas.ebx.dataservices_1.ProgramType.Root.Program;

class QueryTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(QueryTests.class);
  private final Querier querier;

  @BeforeAll
  static void setDefaultAuthenticator() throws Exception {
    AuthenticatorHelper.setDefaultAuthenticator();
  }

  QueryTests() {
    querier = Querier.instance();
  }

  @Test
  void testRelativePredicate() throws Exception {
    final String predicate = "../../root/Mention/mentionID = '" + M1AltBuilder.MENTION_ID + "'";
    final List<Mention> mentions = querier.getMentions(predicate);
    assertEquals(1, mentions.size());
    assertEquals(M1AltBuilder.MENTION_ID, Iterables.getOnlyElement(mentions).getMentionID());
    assertEquals("Master - Informatique",
        Iterables.getOnlyElement(mentions).getName().getValue().getFr().getValue());
  }

  @Test
  void testOrOnePredicate() throws Exception {
    final List<Person> persons =
        querier.getPersons(String.format("personID = '%s' or personID = '%s'",
            "FRUAI0750736TPEIN7547", "FRUAI0750736TPEIN7547"));
    assertTrue(persons.size() == 1);
    LOGGER.info("Person: {}.", Iterables.getOnlyElement(persons).getFamilyName().getValue());
  }

  @Test
  void testOrTwoPredicate() throws Exception {
    final List<Person> persons =
        querier.getPersons(String.format("personID = '%s' or personID = '%s'",
            "FRUAI0750736TPEIN7547", "FRUAI0750736TPEIN7548"));
    assertTrue(persons.size() == 2);
  }

  @Test
  void testOrTwoAndNonePredicate() throws Exception {
    final List<Person> persons =
        querier.getPersons(String.format("personID = '%s' or personID = '%s' or personID = '%s'",
            "FRUAI0750736TPEIN7547", "FRUAI0750736TPEIN7548", "FRUAI0750736TPEIN7548NOTEXISTS"));
    assertTrue(persons.size() == 2);
  }

  @Test
  void testVariousPredicates() throws Exception {
    {
      final List<Program> programs =
          querier.getPrograms("refMention/mentionID='" + M1AltBuilder.MENTION_ID + "'");
      final ImmutableList<String> programIds = programs.stream().map(Program::getIdent)
          .map(JAXBElement::getValue).collect(ImmutableList.toImmutableList());
      assertTrue(programs.size() >= 10, programIds.toString());
    }
    {
      final List<Program> programs =
          querier.getPrograms("programID='" + M1AltBuilder.PROGRAM_ID_S1 + "'");
      assertTrue(programs.size() == 1);
      // final Program program = Iterables.getOnlyElement(programs);
      // LOGGER.info("Program: {}.", program.getIdent().getValue());
      // final List<String> subPrograms =
      // program.getProgramStructure().getValue().getRefProgram();
      // LOGGER.info("Sub program: {}.", subPrograms);
    }
    {
      final List<Program> programs = querier.getPrograms("starts-with(refMention/mentionID, 'FR')");
      assertTrue(programs.size() >= 10);
    }
    {
      final List<Person> persons =
          querier.getPersons(String.format("personID = '%s'", "FRUAI0750736TPEIN7547"));
      assertEquals(ImmutableList.of("CAILLOUX"),
          persons.stream().map(p -> M1AltBuilder.valueOpt(p.getFamilyName()))
              .map(o -> o.orElse(null)).collect(ImmutableList.toImmutableList()));
    }
    {
      final List<Program> programs =
          querier.getPrograms("not(starts-with(refMention/mentionID, 'FR'))");
      assertTrue(programs.size() == 0);
    }
  }

  @Test
  void testCourseJavaObject() throws Exception {
    final Course course = querier.getCourse("FRUAI0750736TCOENA3AMIA-100-S6L1C1");
    assertEquals("Java-Objet", course.getCourseName().getValue().getFr().getValue());
    // assertEquals(M1AltBuilder.MAIN_MANAGER_4_PERSON_ID, course.getManagingTeacher().getValue());
    assertEquals(ImmutableList.of(), course.getTeachers());
    final JAXBElement<Contacts> contactsElement = course.getContacts();
    assertNotNull(contactsElement);
    final Contacts contacts = contactsElement.getValue();
    List<String> refPersons = contacts.getRefPerson();
    assertEquals(1, refPersons.size(), refPersons.toString());
    String refPerson = Iterables.getOnlyElement(refPersons);
    Person person = querier.getPerson(refPerson);
    assertTrue(ImmutableSet.of("CAILLOUX", "BLIDAOUI").contains(person.getFamilyName().getValue()));
    // final String caillouxId = "FRUAI0750736TPEIN7547";
    // assertEquals(caillouxId, refPerson);
    // assertEquals(ImmutableList.of(caillouxId), contacts.getRefPerson());
    // final Person cailloux = querier.getPerson(caillouxId);
    // assertEquals("Cailloux".toUpperCase(), cailloux.getFamilyName().getValue());
    // assertEquals("Olivier".toUpperCase(), cailloux.getGivenName().getValue());
  }

  @Test
  void testForeignKeyPredicateAndProgram() throws Exception {
    /*
     * NB references next to refMention are limited to primary keys, see Extraction de clés
     * étrangères.
     */
    final String predicate =
        "ident = '" + M1AltBuilder.PROGRAM_IDENT + "' and refMention = '" + M1AltBuilder.MENTION_ID
            + "' and refMention/mentionID = '" + M1AltBuilder.MENTION_ID + "'";

    final List<Program> programs = querier.getPrograms(predicate);
    assertEquals(1, programs.size());
    final Program program = Iterables.getOnlyElement(programs);
    assertEquals(M1AltBuilder.PROGRAM_IDENT, program.getIdent().getValue());
    assertEquals(M1AltBuilder.PROGRAM_NAME, program.getProgramName().getValue().getFr().getValue());
    final List<String> subPrograms = program.getProgramStructure().getValue().getRefProgram();
    LOGGER.info("Sub program: {}.", subPrograms);
    assertEquals(2, subPrograms.size());
    assertTrue(subPrograms.get(0).endsWith("-100-S1"));
    assertTrue(subPrograms.get(1).endsWith("-100-S2"));
  }

  @Test
  void testGetCoursesSemester1() throws Exception {
    final String programId = M1AltBuilder.PROGRAM_ID_S1_L1;
    final Program program = querier.getProgram(programId);
    assertEquals(M1AltBuilder.S1_L1_NAME, program.getProgramName().getValue().getFr().getValue());
    assertEquals(M1AltBuilder.MENTION_ID, program.getRefMention().getValue());
    final List<String> subPrograms = program.getProgramStructure().getValue().getRefProgram();
    assertEquals(0, subPrograms.size());
    final List<String> courses = program.getProgramStructure().getValue().getRefCourse();
    assertFalse(courses.isEmpty());
  }
}
