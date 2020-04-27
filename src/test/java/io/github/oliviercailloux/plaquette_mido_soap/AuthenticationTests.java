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
		if (lines.length != 0) {
			Files.write(filePath, ImmutableList.copyOf(lines));
		} else {
			Files.createFile(filePath);
		}
		return filePath;
	}

	Path getNonExistentFile() {
		return jimfs.getPath("Nonexistent.txt");
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@SetSystemProperty(key = "API_password", value = "prop password")
	@Test
	public void testPropReadAuthentication() throws Exception {
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@SetSystemProperty(key = "API_password", value = "prop password")
	@Test
	public void testHalfEnvAndPropReadAuthentication() throws Exception {
		QueriesHelper.env = Map.of("API_username", "env username");
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@Test
	public void testHalfPropAndEnvReadAuthentication() throws Exception {
		QueriesHelper.env = Map.of("API_username", "env username", "API_password", "env password");
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("env username", myAuth.getUsername().get());
		assertEquals("env password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@Test
	public void testHalfPropAndHalfEnvAndFullFileReadAuthentication() throws Exception {
		QueriesHelper.env = Map.of("API_username", "env username");
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@Test
	public void testNoPasswordReadAuthentication() throws Exception {
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "");
		QueriesHelper.env = Map.of("API_username", "env username");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertTrue(myAuth.getPassword().isEmpty());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@SetSystemProperty(key = "API_password", value = "prop password")
	@Test
	public void testPropAndEnvReadAuthentication() throws Exception {
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@Test
	public void testFileReadAuthentication() throws Exception {
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@Test
	public void testEmptyUsernameFileReadAuthentication() throws Exception {
		QueriesHelper.apiLoginFile = createApiLoginFile("", "file password");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertTrue(myAuth.getUsername().isEmpty());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@Test
	public void testNoPasswordFileReadAuthentication() throws Exception {
		QueriesHelper.apiLoginFile = createApiLoginFile("file username");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertTrue(myAuth.getPassword().isEmpty());
	}

	@Test
	public void testEmptySecondLineFileReadAuthentication() throws Exception {
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertTrue(myAuth.getPassword().isEmpty());
	}

	@Test
	public void testIncorrectFileReadAuthentication() throws Exception {
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password", "garbage");

		final Exception exception = assertThrows(IllegalStateException.class, () -> QueriesHelper.readAuthentication());
		assertEquals("File API_login.txt is too long: 3 lines", exception.getMessage());
	}

	@Test
	public void testEmptyThirdLineFileReadAuthentication() throws Exception {
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password", "");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@Test
	public void testEmptyMoreLinesFileReadAuthentication() throws Exception {
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password", "", "");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@Test
	public void testGarbageLaterFileReadAuthentication() throws Exception {
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password", "", "Garbage");

		final Exception exception = assertThrows(IllegalStateException.class, () -> QueriesHelper.readAuthentication());
		assertEquals("File API_login.txt is too long: 4 lines", exception.getMessage());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	public void testHalfPropHaldEnvAndFileReadAuthentication() throws IOException {
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password");
		QueriesHelper.env = Map.of("API_username", "env username");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@SetSystemProperty(key = "API_password", value = "prop password")
	@Test
	public void testPropEnvAndFileReadAuthentication() throws Exception {
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "file password");
		QueriesHelper.env = Map.of("API_username", "env username", "API_password", "env password");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@Test
	public void testNoneGetAuthenticator() throws Exception {
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Login information not found.", exception.getMessage());
	}

	@Test
	public void testEnvNoPasswordGetAuthenticator() throws Exception {
		QueriesHelper.env = Map.of("API_username", "env username");
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Found username but no password.", exception.getMessage());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@Test
	public void testPropAndEnvUserNameNoPasswordGetAuthenticator() throws Exception {
		QueriesHelper.env = Map.of("API_username", "env username");
		QueriesHelper.apiLoginFile = getNonExistentFile();

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Found username but no password.", exception.getMessage());
	}

	@Test
	public void testEmptyUsernameGetAuthenticator() throws Exception {
		QueriesHelper.apiLoginFile = createApiLoginFile("\nfile password");

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Found password but no username.", exception.getMessage());
	}

	@Test
	public void testNoPasswordFileGetAuthenticator() throws Exception {
		QueriesHelper.apiLoginFile = createApiLoginFile("file username", "");

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Found username but no password.", exception.getMessage());
	}
	
	@Test
	public void testEmptyFileGetAuthenticator() throws Exception {
		QueriesHelper.apiLoginFile = createApiLoginFile();
		
		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Login information not found.", exception.getMessage());
	}

	@Test
	public void testEmptyLinesFileGetAuthenticator() throws Exception {
QueriesHelper.apiLoginFile = createApiLoginFile("","","","","");
		
		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Login information not found.", exception.getMessage());
	}
	
	@AfterEach
	void closeJimFs() throws IOException {
		jimfs.close();
	}

}