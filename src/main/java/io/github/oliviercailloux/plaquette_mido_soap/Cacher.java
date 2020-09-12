package io.github.oliviercailloux.plaquette_mido_soap;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import ebx.ebx_dataservices.StandardException;
import schemas.ebx.dataservices_1.CourseType.Root.Course;
import schemas.ebx.dataservices_1.CourseType.Root.Course.Contacts;
import schemas.ebx.dataservices_1.PersonType.Root.Person;
import schemas.ebx.dataservices_1.ProgramType.Root.Program;

public class Cacher {
	public static Cacher cache(Set<String> programIds) throws StandardException {
		final Querier querier = Querier.instance();
		final ImmutableList<Program> programs = querier.getPrograms(programIds);
		final ImmutableSet<String> courseIds = programs.stream()
				.flatMap(p -> p.getProgramStructure().getValue().getRefCourse().stream())
				.collect(ImmutableSet.toImmutableSet());
		final ImmutableList<Course> courses = querier.getCourses(courseIds);
		final ImmutableSet<String> teacherIds = courses.stream().flatMap(c -> getTeacherRefs(c).stream())
				.collect(ImmutableSet.toImmutableSet());
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

	private Cacher(List<Program> programs, List<Course> courses, List<Person> teachers) {
		this.programs = programs.stream().collect(ImmutableBiMap.toImmutableBiMap(Program::getProgramID, p -> p));
		this.courses = courses.stream().collect(ImmutableBiMap.toImmutableBiMap(Course::getCourseID, c -> c));
		this.teachers = teachers.stream().collect(ImmutableBiMap.toImmutableBiMap(Person::getPersonID, p -> p));
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
		final List<String> courseRefs = getProgram(programId).getProgramStructure().getValue().getRefCourse();
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
