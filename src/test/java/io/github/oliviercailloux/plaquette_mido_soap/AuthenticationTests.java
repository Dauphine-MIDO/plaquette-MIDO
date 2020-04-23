package io.github.oliviercailloux.plaquette_mido_soap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

class AuthenticationTests {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTests.class);

	private static Path createApiLoginFile(List<String> lines) throws IOException {
		FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
		Path filePath = fileSystem.getPath("API_login.txt");
		Files.write(filePath, lines);
		return filePath;
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@SetSystemProperty(key = "API_password", value = "prop password")
	@Test
	public void testPropReadAuthentication() throws Exception {
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@SetSystemProperty(key = "API_password", value = "prop password")
	@Test
	public void testHalfEnvAndPropReadAuthentication() throws Exception {
		QueriesHelper.env = Map.of("API_username", "env username");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@Test
	public void testHalfPropAndEnvReadAuthentication() throws Exception {
		QueriesHelper.env = Map.of("API_username", "env username", "API_password", "env password");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("env username", myAuth.getUsername().get());
		assertEquals("env password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@Test
	public void testNoPasswordReadAuthentication() throws Exception {
		QueriesHelper.env = Map.of("API_username", "env username");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertTrue(myAuth.getPassword().isEmpty(), myAuth.getPassword().toString());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@SetSystemProperty(key = "API_password", value = "prop password")
	@Test
	public void testPropAndEnvReadAuthentication() throws Exception {
		System.setProperty("API_username", "prop username");
		System.setProperty("API_password", "prop password");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@Test
	public void testFileReadAuthentication() throws IOException {
		ImmutableList<String> lines = ImmutableList.of("file username", "file password");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);
		QueriesHelper.env = System.getenv();

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@Test
	public void testEmptyUsernameFileReadAuthentication() throws IOException {
		List<String> lines = ImmutableList.of("", "file password");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);
		QueriesHelper.env = System.getenv();

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@Test
	public void testNoPasswordFileReadAuthentication() throws IOException {
		List<String> lines = ImmutableList.of("file username");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);
		QueriesHelper.env = System.getenv();

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertTrue(myAuth.getPassword().isEmpty());
	}

	@Test
	public void testEmptyPasswordFileReadAuthentication() throws IOException {
		List<String> lines = ImmutableList.of("file username", "");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);
		QueriesHelper.env = System.getenv();

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("", myAuth.getPassword().get());
	}

	@Test
	public void testIncorrectFileReadAuthentication() throws IOException {
		List<String> lines = ImmutableList.of("file username", "file password", "garbage");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);
		QueriesHelper.env = System.getenv();

		final Exception exception = assertThrows(IllegalStateException.class, () -> QueriesHelper.readAuthentication());
		assertEquals("File API_login.txt is too long: 3 lines", exception.getMessage());
	}

	@Test
	public void testEmptyThirdLineFileReadAuthentication() throws IOException {
		List<String> lines = ImmutableList.of("file username", "file password", "");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);
		QueriesHelper.env = System.getenv();

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@Test
	public void testNoneGetAuthenticator() throws Exception {
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Login information not found.", exception.getMessage());
	}

	@Test
	public void testNoPasswordGetAuthenticator() throws Exception {
		QueriesHelper.env = Map.of("API_username", "env username");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Found username 'env username' but no password.", exception.getMessage());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@Test
	public void testPropAndEnvUserNameNoPasswordGetAuthenticator() throws Exception {
		QueriesHelper.env = Map.of("API_username", "env username");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Found username 'prop username' but no password.", exception.getMessage());
	}

	@Test
	public void testEmptyUsernameGetAuthenticator() throws Exception {
		List<String> lines = ImmutableList.of("\nfile password");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);
		QueriesHelper.env = System.getenv();

		assertDoesNotThrow(() -> QueriesHelper.setDefaultAuthenticator());
	}

	@Test
	public void testNoPasswordFileGetAuthenticator() throws Exception {
		List<String> lines = ImmutableList.of("file username");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);
		QueriesHelper.env = System.getenv();

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Found username 'file username' but no password.", exception.getMessage());
	}

}