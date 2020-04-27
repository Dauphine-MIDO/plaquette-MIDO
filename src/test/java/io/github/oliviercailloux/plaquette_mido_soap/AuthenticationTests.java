package io.github.oliviercailloux.plaquette_mido_soap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Jimfs;

class AuthenticationTests {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTests.class);
	private FileSystem jimfs;

	@BeforeEach
	void initJimFs() {
		jimfs = Jimfs.newFileSystem();
	}

	@BeforeEach
	void resetQueriesHelper() {
		QueriesHelper.apiLoginFile = Path.of("API_login.txt");
		QueriesHelper.env = System.getenv();
	}

	Path createApiLoginFile(String... lines) throws IOException {
		Path filePath = jimfs.getPath("API_login.txt");
		/** If lines is empty, an empty file gets created. */
		Files.write(filePath, ImmutableList.copyOf(lines));
		return filePath;
	}

	Path getNonExistentFile() {
		return jimfs.getPath("Nonexistent.txt");
	}

	@SetSystemProperty(key = QueriesHelper.USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = QueriesHelper.PASSWORD_KEY, value = "prop password")
	@Test
	public void testPropReadAuthentication() throws Exception {
		QueriesHelper.env = System.getenv();
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = QueriesHelper.USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = QueriesHelper.PASSWORD_KEY, value = "prop password")
	@Test
	public void testHalfEnvAndPropReadAuthentication() throws Exception {
		QueriesHelper.env = Map.of(QueriesHelper.USERNAME_KEY, "env username");
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = QueriesHelper.USERNAME_KEY, value = "prop username")
	@Test
	public void testHalfPropAndEnvReadAuthentication() throws Exception {
		QueriesHelper.env = Map.of(QueriesHelper.USERNAME_KEY, "env username", QueriesHelper.PASSWORD_KEY,
				"env password");
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("env username", myAuth.getUsername().get());
		assertEquals("env password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = QueriesHelper.USERNAME_KEY, value = "prop username")
	@Test
	public void testHalfPropAndHalfEnvAndFullFileReadAuthentication() throws Exception {
		QueriesHelper.env = Map.of(QueriesHelper.USERNAME_KEY, "env username");
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = QueriesHelper.USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = QueriesHelper.PASSWORD_KEY, value = "prop password")
	@Test
	public void testPropAndEnvReadAuthentication() throws Exception {
		QueriesHelper.env = Map.of(QueriesHelper.USERNAME_KEY, "env username", QueriesHelper.PASSWORD_KEY,
				"env password");
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = QueriesHelper.USERNAME_KEY, value = "")
	@SetSystemProperty(key = QueriesHelper.PASSWORD_KEY, value = "")
	@Test
	public void testPropSetToEmptyStringsReadAuthentication() throws Exception {
		QueriesHelper.env = System.getenv();
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("", myAuth.getUsername().get());
		assertEquals("", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = QueriesHelper.USERNAME_KEY, value = "prop username")
	@Test
	public void testNoPasswordReadAuthentication() throws Exception {
		QueriesHelper.env = Map.of(QueriesHelper.USERNAME_KEY, "env username");
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertTrue(myAuth.getPassword().isEmpty(), myAuth.getPassword().toString());
	}

	@Test
	public void testFileReadAuthentication() throws Exception {
		{
			QueriesHelper.env = System.getenv();
			QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password");

			final LoginOpt myAuth = QueriesHelper.readAuthentication();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
		{
			QueriesHelper.env = System.getenv();
			QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password", "");

			final LoginOpt myAuth = QueriesHelper.readAuthentication();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
		{
			QueriesHelper.env = System.getenv();
			QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password", "", "");

			final LoginOpt myAuth = QueriesHelper.readAuthentication();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
	}

	@Test
	public void testEmptyFile() throws Exception {
		QueriesHelper.env = System.getenv();
		{
			QueriesHelper.apiLoginFile = createApiLoginFile();

			final LoginOpt myAuth = QueriesHelper.readAuthentication();

			assertEquals("", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			QueriesHelper.apiLoginFile = createApiLoginFile("", "", "", "", "");

			final LoginOpt myAuth = QueriesHelper.readAuthentication();

			assertEquals("", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			QueriesHelper.apiLoginFile = createApiLoginFile();
			assertDoesNotThrow(() -> QueriesHelper.setDefaultAuthenticator());
		}
		{
			QueriesHelper.apiLoginFile = createApiLoginFile("", "", "", "", "");
			assertDoesNotThrow(() -> QueriesHelper.setDefaultAuthenticator());
		}
	}

	@Test
	public void testEmptyUsernameFile() throws Exception {
		{
			QueriesHelper.env = System.getenv();
			QueriesHelper.apiLoginFile = createApiLoginFile("", "file password");

			final LoginOpt myAuth = QueriesHelper.readAuthentication();

			assertEquals("", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
		{
			QueriesHelper.env = System.getenv();
			QueriesHelper.apiLoginFile = createApiLoginFile("", "file password");

			assertDoesNotThrow(() -> QueriesHelper.setDefaultAuthenticator());
		}
	}

	@Test
	public void testEmptyPasswordFile() throws Exception {
		QueriesHelper.env = System.getenv();
		{
			QueriesHelper.apiLoginFile = createApiLoginFile("file username");

			final LoginOpt myAuth = QueriesHelper.readAuthentication();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			QueriesHelper.apiLoginFile = createApiLoginFile("file username", "");

			final LoginOpt myAuth = QueriesHelper.readAuthentication();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			QueriesHelper.apiLoginFile = createApiLoginFile("file username", "", "");

			final LoginOpt myAuth = QueriesHelper.readAuthentication();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			QueriesHelper.apiLoginFile = createApiLoginFile("file username");
			assertDoesNotThrow(() -> QueriesHelper.setDefaultAuthenticator());
		}
		{
			QueriesHelper.apiLoginFile = createApiLoginFile("file username", "");
			assertDoesNotThrow(() -> QueriesHelper.setDefaultAuthenticator());
		}
		{
			QueriesHelper.apiLoginFile = createApiLoginFile("file username", "", "", "");
			assertDoesNotThrow(() -> QueriesHelper.setDefaultAuthenticator());
		}
	}

	@Test
	public void testIncorrectFileReadAuthentication() throws Exception {
		QueriesHelper.env = System.getenv();
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password", "garbage");

		final Exception exception = assertThrows(IllegalStateException.class, () -> QueriesHelper.readAuthentication());
		assertEquals("File API_login.txt is too long: 3 lines", exception.getMessage());
	}

	@Test
	public void testGarbageLaterFileReadAuthentication() throws Exception {
		QueriesHelper.env = System.getenv();
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password", "", "Garbage");

		final Exception exception = assertThrows(IllegalStateException.class, () -> QueriesHelper.readAuthentication());
		assertEquals("File API_login.txt is too long: 4 lines", exception.getMessage());
	}

	@SetSystemProperty(key = QueriesHelper.USERNAME_KEY, value = "prop username")
	public void testHalfPropHalfEnvAndFileReadAuthentication() throws IOException {
		QueriesHelper.env = Map.of(QueriesHelper.USERNAME_KEY, "env username");
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = QueriesHelper.USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = QueriesHelper.PASSWORD_KEY, value = "prop password")
	@Test
	public void testPropEnvAndFileReadAuthentication() throws Exception {
		QueriesHelper.env = Map.of(QueriesHelper.USERNAME_KEY, "env username", QueriesHelper.PASSWORD_KEY,
				"env password");
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@Test
	public void testNoneGetAuthenticator() throws Exception {
		QueriesHelper.env = System.getenv();
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Login information not found.", exception.getMessage());
	}

	@Test
	public void testHalfEnvGetAuthenticator() throws Exception {
		QueriesHelper.env = Map.of(QueriesHelper.USERNAME_KEY, "env username");
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Found username but no password.", exception.getMessage());
	}

	@SetSystemProperty(key = QueriesHelper.USERNAME_KEY, value = "prop username")
	@Test
	public void testHalfPropAndHalfEnvGetAuthenticator() throws Exception {
		QueriesHelper.env = Map.of(QueriesHelper.USERNAME_KEY, "env username");
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Found username but no password.", exception.getMessage());
	}

	@AfterEach
	void closeJimFs() throws IOException {
		jimfs.close();
	}

}