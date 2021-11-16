package io.github.oliviercailloux.plaquette_mido_soap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import ebx.ebx_dataservices.StandardException;
import jakarta.xml.bind.JAXBElement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schemas.ebx.dataservices_1.CourseType.Root.Course;
import schemas.ebx.dataservices_1.CourseType.Root.Course.Contacts;
import schemas.ebx.dataservices_1.PersonType.Root.Person;
import schemas.ebx.dataservices_1.ProgramType.Root.Program;

public class Cacher {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(Cacher.class);

  public static Cacher cache(Querier querier, Set<String> programIds) throws StandardException {
    final ImmutableSet.Builder<Program> builder = ImmutableSet.builder();
    final Set<String> programIdsSeen = new LinkedHashSet<>();
    ImmutableSet<String> nextIds = ImmutableSet.copyOf(programIds);
    do {
      final ImmutableList<Program> programs = querier.getPrograms(nextIds);
      builder.addAll(programs);
      programs.stream().map(p -> p.getProgramID()).forEach(programIdsSeen::add);
      LOGGER.debug("Program ids seen: {}.", programIdsSeen);
      ImmutableSet<String> subProgramIds = programs.stream()
          .flatMap(p -> p.getProgramStructure().getValue().getRefProgram().stream())
          .collect(ImmutableSet.toImmutableSet());
      nextIds = Sets.difference(subProgramIds, programIds).immutableCopy();
      verify(Sets.intersection(nextIds, programIdsSeen).isEmpty());
    } while (!nextIds.isEmpty());
    final ImmutableSet<Program> programs = builder.build();
    LOGGER.debug("Programs: {}.",
        programs.stream().map(p -> p.getProgramName().getValue().getFr().getValue())
            .collect(Collectors.joining(", ")));
    LOGGER.debug("Programs and courses: {}.", programs.stream()
        .map(p -> p.getProgramName().getValue().getFr().getValue() + ": " + p.getProgramStructure()
            .getValue().getRefCourse().stream().collect(Collectors.joining(", ")))
        .collect(Collectors.joining("; ")));
    final ImmutableSet<String> courseIds =
        programs.stream().flatMap(p -> p.getProgramStructure().getValue().getRefCourse().stream())
            .collect(ImmutableSet.toImmutableSet());
    LOGGER.debug("Course ids: {}.", courseIds);
    final ImmutableList<Course> courses = querier.getCourses(courseIds);
    LOGGER.debug("Courses: {}.",
        courses.stream()
            .map(c -> c.getCourseID() + " - " + c.getCourseName().getValue().getFr().getValue())
            .collect(Collectors.joining(", ")));
    final ImmutableSet<String> teacherIds = courses.stream()
        .flatMap(c -> getTeacherRefs(c).stream()).collect(ImmutableSet.toImmutableSet());
    final ImmutableList<Person> teachers = querier.getPersons(teacherIds);
    return new Cacher(programs, courses, teachers);
  }

  private static <T> Optional<T> valueOpt(JAXBElement<T> element) {
    return element == null ? Optional.empty() : Optional.of(element.getValue());
  }

  private static List<String> getTeacherRefs(Course course) {
    return valueOpt(course.getContacts()).map(Contacts::getRefPerson).orElse(ImmutableList.of());
  }

  private final ImmutableBiMap<String, Program> programs;
  private final ImmutableBiMap<String, Course> courses;
  private final ImmutableBiMap<String, Person> teachers;

  private Cacher(Set<Program> programs, List<Course> courses, List<Person> teachers) {
    this.programs =
        programs.stream().collect(ImmutableBiMap.toImmutableBiMap(Program::getProgramID, p -> p));
    this.courses =
        courses.stream().collect(ImmutableBiMap.toImmutableBiMap(Course::getCourseID, c -> c));
    this.teachers =
        teachers.stream().collect(ImmutableBiMap.toImmutableBiMap(Person::getPersonID, p -> p));
  }

  public ImmutableMap<String, Program> getPrograms() {
    return programs;
  }

  public Program getProgram(String programId) {
    checkArgument(programs.containsKey(programId));
    return programs.get(programId);
  }

  public ImmutableMap<String, Course> getCourses() {
    return courses;
  }

  public ImmutableMap<String, Course> getProgramCourses(String programId) {
    final List<String> courseRefs =
        getProgram(programId).getProgramStructure().getValue().getRefCourse();
    return ImmutableMap.copyOf(Maps.filterKeys(courses, courseRefs::contains));
  }

  public Course getCourse(String courseId) {
    checkArgument(courses.containsKey(courseId));
    return courses.get(courseId);
  }

  public ImmutableMap<String, Person> getTeachers() {
    return teachers;
  }

  public ImmutableBiMap<String, Person> getCourseTeachers(String courseId) {
    final List<String> teacherRefs = getTeacherRefs(getCourse(courseId));
    return ImmutableBiMap.copyOf(Maps.filterKeys(teachers, teacherRefs::contains));
  }

  public Person getTeacher(String teacherId) {
    checkArgument(teachers.containsKey(teacherId));
    return teachers.get(teacherId);
  }
}
