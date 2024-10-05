package io.github.oliviercailloux.plaquette;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import de.siegmar.fastcsv.writer.CsvWriter;
import ebx.ebx_dataservices.StandardException;
import io.github.oliviercailloux.publish.AsciidocWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schemas.ebx.dataservices_1.CourseType.Root.Course;

public class M1AltTable {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(M1AltBuilder.class);

  public static final String MENTION_ID = "FRUAI0750736TPRMEA5IFO";

  public static final String PROGRAM_IDENT = "PRA4AMIA-100";

  public static final String PROGRAM_ID_PREFIX = "FRUAI0750736TPR";

  public static final String PROGRAM_ID = PROGRAM_ID_PREFIX + PROGRAM_IDENT;

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
    AuthenticatorHelper.setDefaultAuthenticator();

    final M1AltTable builder = new M1AltTable();
    builder.proceed();
  }

  private Cacher cache;

  private final Querier querier;

  public M1AltTable() {
    cache = null;
    querier = Querier.instance();
  }

  private void proceed() throws StandardException, IOException {
    final ImmutableSet<String> programs = ImmutableSet.of(PROGRAM_ID, PROGRAM_ID_S1,
        PROGRAM_ID_S1_L1, PROGRAM_ID_S2, PROGRAM_ID_S2_L1, PROGRAM_ID_S2_L2);
    cache = Cacher.cache(querier, programs);

    Path file = Paths.get("out.csv");
    try (CsvWriter csv = CsvWriter.builder().build(file)) {
      csv.writeRecord("Title", "ECTS");
      
      ImmutableCollection<Course> courses = cache.getCourses().values();
      for (Course course : courses) {
        csv.writeRecord(course.getCourseName().getValue().getFr().getValue(), course.getEcts().getValue());
      }
    }
  }
}
