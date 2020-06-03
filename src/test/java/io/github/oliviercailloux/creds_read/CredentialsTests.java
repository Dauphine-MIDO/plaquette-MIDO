package io.github.oliviercailloux.creds_read;

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

import io.github.oliviercailloux.creds_read.CredsOpt;
import io.github.oliviercailloux.creds_read.CredsReader;
import io.github.oliviercailloux.plaquette_mido_soap.QueriesHelper;

class CredentialsTests {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsTests.class);
	private FileSystem jimfs;

	@BeforeEach
	void initJimFs() {
		jimfs = Jimfs.newFileSystem();
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

	@SetSystemProperty(key = CredsReader.DEFAULT_USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = CredsReader.DEFAULT_PASSWORD_KEY, value = "prop password")
	@Test
	public void testPropReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY, CredsReader.DEFAULT_PASSWORD_KEY,
				getNonExistentFile());

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = CredsReader.DEFAULT_USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = CredsReader.DEFAULT_PASSWORD_KEY, value = "prop password")
	@Test
	public void testPropAndEnvReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY, CredsReader.DEFAULT_PASSWORD_KEY,
				getNonExistentFile());
		credsReader.setEnv(Map.of(CredsReader.DEFAULT_USERNAME_KEY, "env username", CredsReader.DEFAULT_PASSWORD_KEY,
				"env password"));

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}
	
	@SetSystemProperty(key = CredsReader.DEFAULT_USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = CredsReader.DEFAULT_PASSWORD_KEY, value = "prop password")
	@Test
	public void testHalfEnvAndPropReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY, CredsReader.DEFAULT_PASSWORD_KEY,
				getNonExistentFile());
		credsReader.setEnv(Map.of(CredsReader.DEFAULT_USERNAME_KEY, "env username"));

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = CredsReader.DEFAULT_USERNAME_KEY, value = "prop username")
	@Test
	public void testHalfPropAndEnvReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY, CredsReader.DEFAULT_PASSWORD_KEY,
				getNonExistentFile());
		credsReader.setEnv(Map.of(CredsReader.DEFAULT_USERNAME_KEY, "env username", CredsReader.DEFAULT_PASSWORD_KEY,
				"env password"));

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("env username", myAuth.getUsername().get());
		assertEquals("env password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = CredsReader.DEFAULT_USERNAME_KEY, value = "prop username")
	@Test
	public void testHalfPropAndHalfEnvAndFullFileReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY, CredsReader.DEFAULT_PASSWORD_KEY,
				createApiLoginFile("file username", "file password"));
		credsReader.setEnv(Map.of(CredsReader.DEFAULT_USERNAME_KEY, "env username"));

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}
	
	@SetSystemProperty(key = CredsReader.DEFAULT_USERNAME_KEY, value = "prop username")
	public void testHalfPropHalfEnvAndFileReadCredentials() throws IOException {
		CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY, CredsReader.DEFAULT_PASSWORD_KEY,
				createApiLoginFile("file username", "file password"));
		credsReader.setEnv(Map.of(CredsReader.DEFAULT_USERNAME_KEY, "env username"));

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = CredsReader.DEFAULT_USERNAME_KEY, value = "prop username")
	@SetSystemProperty(key = CredsReader.DEFAULT_PASSWORD_KEY, value = "prop password")
	@Test
	public void testPropEnvAndFileReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY, CredsReader.DEFAULT_PASSWORD_KEY,
				createApiLoginFile("file username", "file password"));
		credsReader.setEnv(Map.of(CredsReader.DEFAULT_USERNAME_KEY, "env username", CredsReader.DEFAULT_PASSWORD_KEY,
				"env password"));

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = CredsReader.DEFAULT_USERNAME_KEY, value = "")
	@SetSystemProperty(key = CredsReader.DEFAULT_PASSWORD_KEY, value = "")
	@Test
	public void testPropSetToEmptyStringsReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY, CredsReader.DEFAULT_PASSWORD_KEY,
				getNonExistentFile());

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("", myAuth.getUsername().get());
		assertEquals("", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = CredsReader.DEFAULT_USERNAME_KEY, value = "prop username")
	@Test
	public void testNoPasswordReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY, CredsReader.DEFAULT_PASSWORD_KEY,
				getNonExistentFile());
		credsReader.setEnv(Map.of(CredsReader.DEFAULT_USERNAME_KEY, "env username"));

		final CredsOpt myAuth = credsReader.readCredentials();

		assertEquals("prop username", myAuth.getUsername().get());
		assertTrue(myAuth.getPassword().isEmpty(), myAuth.getPassword().toString());
	}

	@Test
	public void testFileReadCredentials() throws Exception {
		{
			CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY,
					CredsReader.DEFAULT_PASSWORD_KEY, createApiLoginFile("file username", "file password"));

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY,
					CredsReader.DEFAULT_PASSWORD_KEY, createApiLoginFile("file username", "file password", ""));

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY,
					CredsReader.DEFAULT_PASSWORD_KEY, createApiLoginFile("file username", "file password", "", ""));

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
	}

	@Test
	public void testEmptyFile() throws Exception {
		{
			CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY,
					CredsReader.DEFAULT_PASSWORD_KEY, createApiLoginFile());

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY,
					CredsReader.DEFAULT_PASSWORD_KEY, createApiLoginFile("", "", "", "", ""));

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY,
					CredsReader.DEFAULT_PASSWORD_KEY, createApiLoginFile());
			assertDoesNotThrow(() -> credsReader.getCredentials());
		}
		{
			CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY,
					CredsReader.DEFAULT_PASSWORD_KEY, createApiLoginFile("", "", "", "", ""));
			assertDoesNotThrow(() -> credsReader.getCredentials());
		}
	}

	@Test
	public void testEmptyUsernameFile() throws Exception {
		{
			CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY,
					CredsReader.DEFAULT_PASSWORD_KEY, createApiLoginFile("", "file password"));

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("", myAuth.getUsername().get());
			assertEquals("file password", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY,
					CredsReader.DEFAULT_PASSWORD_KEY, createApiLoginFile("", "file password"));

			assertDoesNotThrow(() -> credsReader.getCredentials());
		}
	}

	@Test
	public void testEmptyPasswordFile() throws Exception {
		{
			CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY,
					CredsReader.DEFAULT_PASSWORD_KEY, createApiLoginFile("file username"));

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY,
					CredsReader.DEFAULT_PASSWORD_KEY, createApiLoginFile("file username", ""));

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY,
					CredsReader.DEFAULT_PASSWORD_KEY, createApiLoginFile("file username", "", ""));

			final CredsOpt myAuth = credsReader.readCredentials();

			assertEquals("file username", myAuth.getUsername().get());
			assertEquals("", myAuth.getPassword().get());
		}
		{
			CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY,
					CredsReader.DEFAULT_PASSWORD_KEY, createApiLoginFile("file username"));
			assertDoesNotThrow(() -> credsReader.getCredentials());
		}
		{
			CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY,
					CredsReader.DEFAULT_PASSWORD_KEY, createApiLoginFile("file username", ""));
			assertDoesNotThrow(() -> credsReader.getCredentials());
		}
		{
			CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY,
					CredsReader.DEFAULT_PASSWORD_KEY, createApiLoginFile("file username", "", "", ""));
			assertDoesNotThrow(() -> credsReader.getCredentials());
		}
	}

	@Test
	public void testIncorrectFileReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY, CredsReader.DEFAULT_PASSWORD_KEY,
				createApiLoginFile("file username", "file password", "garbage"));

		final Exception exception = assertThrows(IllegalStateException.class, () -> credsReader.readCredentials());
		assertEquals("File API_login.txt is too long: 3 lines", exception.getMessage());
	}

	@Test
	public void testGarbageLaterFileReadCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY, CredsReader.DEFAULT_PASSWORD_KEY,
				createApiLoginFile("file username", "file password", "", "Garbage"));

		final Exception exception = assertThrows(IllegalStateException.class, () -> credsReader.readCredentials());
		assertEquals("File API_login.txt is too long: 4 lines", exception.getMessage());
	}

	@Test
	public void testNoneGetCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY, CredsReader.DEFAULT_PASSWORD_KEY,
				getNonExistentFile());

		final Exception exception = assertThrows(IllegalStateException.class, () -> credsReader.getCredentials());
		assertEquals("Login information not found.", exception.getMessage());
	}

	@Test
	public void testHalfEnvGetCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY, CredsReader.DEFAULT_PASSWORD_KEY,
				getNonExistentFile());
		credsReader.setEnv(Map.of(CredsReader.DEFAULT_USERNAME_KEY, "env username"));

		final Exception exception = assertThrows(IllegalStateException.class, () -> credsReader.getCredentials());
		assertEquals("Found username but no password.", exception.getMessage());
	}

	@SetSystemProperty(key = CredsReader.DEFAULT_USERNAME_KEY, value = "prop username")
	@Test
	public void testHalfPropAndHalfEnvGetCredentials() throws Exception {
		CredsReader credsReader = CredsReader.given(CredsReader.DEFAULT_USERNAME_KEY, CredsReader.DEFAULT_PASSWORD_KEY,
				getNonExistentFile());
		credsReader.setEnv(Map.of(CredsReader.DEFAULT_USERNAME_KEY, "env username"));

		final Exception exception = assertThrows(IllegalStateException.class, () -> credsReader.getCredentials());
		assertEquals("Found username but no password.", exception.getMessage());
	}

	@AfterEach
	void closeJimFs() throws IOException {
		jimfs.close();
	}

}