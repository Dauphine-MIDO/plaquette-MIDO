package io.github.oliviercailloux.CredsOpt;

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

import io.github.oliviercailloux.creds_read.CredsReader;
import io.github.oliviercailloux.plaquette_mido_soap.QueriesHelper;


class AuthenticationTests {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTests.class);
	private FileSystem jimfs;

	@BeforeEach
	void initJimFs() {
		jimfs = Jimfs.newFileSystem();
	}

	@BeforeEach
	void resetCredsReader() {
		CredsReader.credsFile = Path.of("API_login.txt");
		CredsReader.env = System.getenv();
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

	@SetSystemProperty(key = CredsReader.USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = CredsReader.PASSWORD_KEY, value = "prop password")
	@Test
	public void testPropReadAuthentication() throws Exception {
		CredsReader.env = System.getenv();
		CredsReader.credsFile = getNonExistentFile();

		final CredsOpt myAuth = CredsReader.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = CredsReader.USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = CredsReader.PASSWORD_KEY, value = "prop password")
	@Test
	public void testHalfEnvAndPropReadAuthentication() throws Exception {
		CredsReader.env = Map.of(CredsReader.USERNAME_KEY, "env username");
		CredsReader.credsFile = getNonExistentFile();

		final CredsOpt myAuth = CredsReader.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = CredsReader.USERNAME_KEY, value = "prop username")
	@Test
	public void testHalfPropAndEnvReadAuthentication() throws Exception {
		CredsReader.env = Map.of(CredsReader.USERNAME_KEY, "env username", CredsReader.PASSWORD_KEY,
				"env password");
		CredsReader.credsFile = getNonExistentFile();

		final CredsOpt myAuth = CredsReader.readAuthentication();

		assertEquals("env username", myAuth.getUsername().get());
		assertEquals("env password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = CredsReader.USERNAME_KEY, value = "prop username")
	@Test
	public void testHalfPropAndHalfEnvAndFullFileReadAuthentication() throws Exception {
		CredsReader.env = Map.of(CredsReader.USERNAME_KEY, "env username");
		CredsReader.credsFile = createApiLoginFile("file username", "file password");

		final CredsOpt myAuth = CredsReader.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = CredsReader.USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = CredsReader.PASSWORD_KEY, value = "prop password")
	@Test
	public void testPropAndEnvReadAuthentication() throws Exception {
		CredsReader.env = Map.of(CredsReader.USERNAME_KEY, "env username", CredsReader.PASSWORD_KEY,
				"env password");
		CredsReader.credsFile = getNonExistentFile();

		final CredsOpt myAuth = CredsReader.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = CredsReader.USERNAME_KEY, value = "")
	@SetSystemProperty(key = CredsReader.PASSWORD_KEY, value = "")
	@Test
	public void testPropSetToEmptyStringsReadAuthentication() throws Exception {
		CredsReader.env = System.getenv();
		CredsReader.credsFile = getNonExistentFile();

		final CredsOpt myAuth = CredsReader.readAuthentication();

		assertEquals("", myAuth.getUsername().get());
		assertEquals("", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = CredsReader.USERNAME_KEY, value = "prop username")
	@Test
	public void testNoPasswordReadAuthentication() throws Exception {
		CredsReader.env = Map.of(CredsReader.USERNAME_KEY, "env username");
		CredsReader.credsFile = getNonExistentFile();

		final CredsOpt myAuth = CredsReader.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertTrue(myAuth.getPassword().isEmpty(), myAuth.getPassword().toString());
	}

	@Test
	public void testFileReadAuthentication() throws Exception {
		{
			CredsReader.env = System.getenv();
			CredsReader.credsFile = createApiLoginFile("file username", "file password");

			final CredsOpt myAuth = CredsReader.readAuthentication();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
		{
			CredsReader.env = System.getenv();
			CredsReader.credsFile = createApiLoginFile("file username", "file password", "");

			final CredsOpt myAuth = CredsReader.readAuthentication();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
		{
			CredsReader.env = System.getenv();
			CredsReader.credsFile = createApiLoginFile("file username", "file password", "", "");

			final CredsOpt myAuth = CredsReader.readAuthentication();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
	}

	@Test
	public void testEmptyFile() throws Exception {
		CredsReader.env = System.getenv();
		{
			CredsReader.credsFile = createApiLoginFile();

			final CredsOpt myAuth = CredsReader.readAuthentication();

			assertEquals("", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			CredsReader.credsFile = createApiLoginFile("", "", "", "", "");

			final CredsOpt myAuth = CredsReader.readAuthentication();

			assertEquals("", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			CredsReader.credsFile = createApiLoginFile();
			assertDoesNotThrow(() -> QueriesHelper.setDefaultAuthenticator());
		}
		{
			CredsReader.credsFile = createApiLoginFile("", "", "", "", "");
			assertDoesNotThrow(() -> QueriesHelper.setDefaultAuthenticator());
		}
	}

	@Test
	public void testEmptyUsernameFile() throws Exception {
		{
			CredsReader.env = System.getenv();
			CredsReader.credsFile = createApiLoginFile("", "file password");

			final CredsOpt myAuth = CredsReader.readAuthentication();

			assertEquals("", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
		{
			CredsReader.env = System.getenv();
			CredsReader.credsFile = createApiLoginFile("", "file password");

			assertDoesNotThrow(() -> QueriesHelper.setDefaultAuthenticator());
		}
	}

	@Test
	public void testEmptyPasswordFile() throws Exception {
		CredsReader.env = System.getenv();
		{
			CredsReader.credsFile = createApiLoginFile("file username");

			final CredsOpt myAuth = CredsReader.readAuthentication();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			CredsReader.credsFile = createApiLoginFile("file username", "");

			final CredsOpt myAuth = CredsReader.readAuthentication();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			CredsReader.credsFile = createApiLoginFile("file username", "", "");

			final CredsOpt myAuth = CredsReader.readAuthentication();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			CredsReader.credsFile = createApiLoginFile("file username");
			assertDoesNotThrow(() -> QueriesHelper.setDefaultAuthenticator());
		}
		{
			CredsReader.credsFile = createApiLoginFile("file username", "");
			assertDoesNotThrow(() -> QueriesHelper.setDefaultAuthenticator());
		}
		{
			CredsReader.credsFile = createApiLoginFile("file username", "", "", "");
			assertDoesNotThrow(() -> QueriesHelper.setDefaultAuthenticator());
		}
	}

	@Test
	public void testIncorrectFileReadAuthentication() throws Exception {
		CredsReader.env = System.getenv();
		CredsReader.credsFile = createApiLoginFile("file username", "file password", "garbage");

		final Exception exception = assertThrows(IllegalStateException.class, () -> CredsReader.readAuthentication());
		assertEquals("File API_login.txt is too long: 3 lines", exception.getMessage());
	}

	@Test
	public void testGarbageLaterFileReadAuthentication() throws Exception {
		CredsReader.env = System.getenv();
		CredsReader.credsFile = createApiLoginFile("file username", "file password", "", "Garbage");

		final Exception exception = assertThrows(IllegalStateException.class, () -> CredsReader.readAuthentication());
		assertEquals("File API_login.txt is too long: 4 lines", exception.getMessage());
	}

	@SetSystemProperty(key = CredsReader.USERNAME_KEY, value = "prop username")
	public void testHalfPropHalfEnvAndFileReadAuthentication() throws IOException {
		CredsReader.env = Map.of(CredsReader.USERNAME_KEY, "env username");
		CredsReader.credsFile = createApiLoginFile("file username", "file password");

		final CredsOpt myAuth = CredsReader.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = CredsReader.USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = CredsReader.PASSWORD_KEY, value = "prop password")
	@Test
	public void testPropEnvAndFileReadAuthentication() throws Exception {
		CredsReader.env = Map.of(CredsReader.USERNAME_KEY, "env username", CredsReader.PASSWORD_KEY,
				"env password");
		CredsReader.credsFile = createApiLoginFile("file username", "file password");

		final CredsOpt myAuth = CredsReader.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@Test
	public void testNoneGetAuthenticator() throws Exception {
		CredsReader.env = System.getenv();
		CredsReader.credsFile = getNonExistentFile();

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Login information not found.", exception.getMessage());
	}

	@Test
	public void testHalfEnvGetAuthenticator() throws Exception {
		CredsReader.env = Map.of(CredsReader.USERNAME_KEY, "env username");
		CredsReader.credsFile = getNonExistentFile();

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Found username but no password.", exception.getMessage());
	}

	@SetSystemProperty(key = CredsReader.USERNAME_KEY, value = "prop username")
	@Test
	public void testHalfPropAndHalfEnvGetAuthenticator() throws Exception {
		CredsReader.env = Map.of(CredsReader.USERNAME_KEY, "env username");
		CredsReader.credsFile = getNonExistentFile();

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Found username but no password.", exception.getMessage());
	}

	@AfterEach
	void closeJimFs() throws IOException {
		jimfs.close();
	}

}