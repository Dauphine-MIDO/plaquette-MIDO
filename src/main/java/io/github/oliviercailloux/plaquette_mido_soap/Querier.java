package io.github.oliviercailloux.plaquette_mido_soap;

import static com.google.common.base.Verify.verify;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import ebx.ebx_dataservices.EbxDataservices;
import ebx.ebx_dataservices.EbxDataservicesService;
import ebx.ebx_dataservices.StandardException;
import io.github.oliviercailloux.xml_utils.XmlUtils;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schemas.ebx.dataservices_1.CourseType.Root.Course;
import schemas.ebx.dataservices_1.MentionType.Root.Mention;
import schemas.ebx.dataservices_1.ObjectFactory;
import schemas.ebx.dataservices_1.PersonType.Root.Person;
import schemas.ebx.dataservices_1.ProgramType.Root.Program;
import schemas.ebx.dataservices_1.SelectCourseRequestType;
import schemas.ebx.dataservices_1.SelectCourseResponseType;
import schemas.ebx.dataservices_1.SelectMentionRequestType;
import schemas.ebx.dataservices_1.SelectMentionResponseType;
import schemas.ebx.dataservices_1.SelectPersonRequestType;
import schemas.ebx.dataservices_1.SelectPersonResponseType;
import schemas.ebx.dataservices_1.SelectProgramRequestType;
import schemas.ebx.dataservices_1.SelectProgramResponseType;

public class Querier {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(Querier.class);

  private static final Querier INSTANCE = new Querier();

  public static Querier instance() {
    return INSTANCE;
  }

  private final EbxDataservices dataservices;

  private Querier() {
    dataservices = new EbxDataservicesService().getEbxDataservices();
  }

  private String toOrPredicate(String idFieldName, Set<String> ids) {
    final ImmutableList<String> predicates = ids.stream().map(s -> idFieldName + " = '" + s + "'")
        .collect(ImmutableList.toImmutableList());
    final String predicate = predicates.stream().collect(Collectors.joining(" or "));
    return predicate;
  }

  public ImmutableList<Mention> getMentions(String predicate) throws StandardException {
    final SelectMentionRequestType request = new SelectMentionRequestType();
    request.setBranch("pvRefRof");
    request.setInstance("RefRof");
    request.setPredicate(predicate);
    LOGGER.debug("Request: {}.", XmlUtils.toXml(new ObjectFactory().createSelectMention(request)));
    final SelectMentionResponseType result = dataservices.selectMentionOperation(request);
    LOGGER.debug("Result: {}.",
        XmlUtils.toXml(new ObjectFactory().createSelectMentionResponse(result)));
    return ImmutableList.copyOf(result.getData().getRoot().getMention());
  }

  public ImmutableList<Mention> getMentions(Set<String> mentionIds) throws StandardException {
    final ImmutableList<Mention> mentions = getMentions(toOrPredicate("mentionID", mentionIds));
    return reorder(mentionIds, mentions, Mention::getMentionID);
  }

  public Mention getMention(String mentionId) throws StandardException {
    final String predicate = "mentionID = '" + mentionId + "'";
    final List<Mention> mentions = getMentions(predicate);
    Verify.verify(mentions.size() == 1);
    final Mention mention = Iterables.getOnlyElement(mentions);
    Verify.verify(mention.getMentionID().equals(mentionId));
    return mention;
  }

  public ImmutableList<Program> getPrograms(String predicate) throws StandardException {
    final SelectProgramRequestType request = new SelectProgramRequestType();
    request.setBranch("pvRefRof");
    request.setInstance("RefRof");
    request.setPredicate(predicate);
    LOGGER.debug("Request: {}.", XmlUtils.toXml(new ObjectFactory().createSelectProgram(request)));
    final SelectProgramResponseType result = dataservices.selectProgramOperation(request);
    LOGGER.debug("Result: {}.",
        XmlUtils.toXml(new ObjectFactory().createSelectProgramResponse(result)));
    return ImmutableList.copyOf(result.getData().getRoot().getProgram());
  }

  public ImmutableList<Program> getPrograms(Set<String> programIds) throws StandardException {
    final ImmutableList<Program> programs = getPrograms(toOrPredicate("programID", programIds));
    return reorder(programIds, programs, Program::getProgramID);
  }

  public Program getProgram(String programId) throws StandardException {
    final String predicate = "programID = '" + programId + "'";
    final List<Program> programs = getPrograms(predicate);
    Verify.verify(programs.size() == 1);
    final Program program = Iterables.getOnlyElement(programs);
    Verify.verify(program.getProgramID().equals(programId));
    return program;
  }

  public ImmutableList<Course> getCourses(String predicate) throws StandardException {
    final SelectCourseRequestType request = new SelectCourseRequestType();
    request.setBranch("pvRefRof");
    request.setInstance("RefRof");
    request.setPredicate(predicate);
    LOGGER.debug("Request: {}.", XmlUtils.toXml(new ObjectFactory().createSelectCourse(request)));
    final SelectCourseResponseType result = dataservices.selectCourseOperation(request);
    LOGGER.debug("Result: {}.",
        XmlUtils.toXml(new ObjectFactory().createSelectCourseResponse(result)));
    return ImmutableList.copyOf(result.getData().getRoot().getCourse());
  }

  /**
   * @param courseIds
   * @return the courses found (a subset of those searched for), in the same ordering.
   * @throws StandardException
   */
  public ImmutableList<Course> getCourses(Set<String> courseIds) throws StandardException {
    final ImmutableList<Course> courses = getCourses(toOrPredicate("courseID", courseIds));
    /*
     * Re-ordering seems mandatory: I have observed that the service does not always return the
     * courses in the order given in the predicate.
     */
    return reorder(courseIds, courses, Course::getCourseID);
  }

  private <K> ImmutableList<K> reorder(Set<String> orderedIds, ImmutableList<K> matches,
      Function<K, String> getId) {
    final ImmutableBiMap<String, K> matchesFromIds =
        matches.stream().collect(ImmutableBiMap.toImmutableBiMap(getId, Function.identity()));
    verify(orderedIds.containsAll(matchesFromIds.keySet()));
    return orderedIds.stream().filter(matchesFromIds::containsKey).map(matchesFromIds::get)
        .collect(ImmutableList.toImmutableList());
  }

  public Course getCourse(String courseId) throws StandardException {
    final String predicate = "courseID = '" + courseId + "'";
    final List<Course> courses = getCourses(predicate);
    Verify.verify(courses.size() == 1);
    final Course course = Iterables.getOnlyElement(courses);
    Verify.verify(course.getCourseID().equals(courseId));
    return course;
  }

  public ImmutableList<Person> getPersons(String predicate) throws StandardException {
    final SelectPersonRequestType request = new SelectPersonRequestType();
    request.setBranch("pvRefRof");
    request.setInstance("RefRof");
    request.setPredicate(predicate);
    LOGGER.debug("Request: {}.", XmlUtils.toXml(new ObjectFactory().createSelectPerson(request)));
    final SelectPersonResponseType result = dataservices.selectPersonOperation(request);
    LOGGER.debug("Result: {}.",
        XmlUtils.toXml(new ObjectFactory().createSelectPersonResponse(result)));
    return ImmutableList.copyOf(result.getData().getRoot().getPerson());
  }

  public ImmutableList<Person> getPersons(Set<String> personIds) throws StandardException {
    final ImmutableList<Person> persons = getPersons(toOrPredicate("personID", personIds));
    return reorder(personIds, persons, Person::getPersonID);
  }

  public Person getPerson(String personId) throws StandardException {
    final String predicate = "personID = '" + personId + "'";
    final List<Person> persons = getPersons(predicate);
    Verify.verify(persons.size() == 1);
    final Person person = Iterables.getOnlyElement(persons);
    Verify.verify(person.getPersonID().equals(personId));
    return person;
  }
}
