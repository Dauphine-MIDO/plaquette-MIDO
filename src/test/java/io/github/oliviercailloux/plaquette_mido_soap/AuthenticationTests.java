package io.github.oliviercailloux.plaquette_mido_soap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AuthenticationTests {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTests.class);

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
	@Rule
	public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

	@Test
	public void testPropReadAuthentication() throws Exception {
		System.setProperty("API_username", "prop username");
		System.setProperty("API_password", "prop password");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final Authentication myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@Test
	public void testHalfEnvAndPropReadAuthentication() throws Exception {
		environmentVariables.set("API_username", "env username");
		System.setProperty("API_username", "prop username");
		System.setProperty("API_password", "prop password");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final Authentication myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@Test
	public void testHalfPropAndEnvReadAuthentication() throws Exception {
		System.setProperty("API_username", "prop username");
		environmentVariables.set("API_username", "env username");
		environmentVariables.set("API_password", "env password");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final Authentication myAuth = QueriesHelper.readAuthentication();

		assertEquals("env username", myAuth.getUsername().get());
		assertEquals("env password", myAuth.getPassword().get());
	}

	@Test
	public void testNoPasswordReadAuthentication() throws Exception {
		environmentVariables.set("API_username", "env username");
		System.setProperty("API_username", "prop username");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final Authentication myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertTrue(myAuth.getPassword().isEmpty(), myAuth.getPassword().toString());
	}

	@Test
	public void testPropAndEnvReadAuthentication() throws Exception {
		environmentVariables.set("API_username", "env username");
		environmentVariables.set("API_password", "env password");
		System.setProperty("API_username", "prop username");
		System.setProperty("API_password", "prop password");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final Authentication myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@Test
	public void testNoneGetAuthenticator() throws Exception {
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final Exception exception = assertThrows(IllegalStateException.class, () -> QueriesHelper.getAuthenticator());
		assertEquals("Login information not found.", exception.getMessage());
	}

	@Test
	public void testNoPasswordGetAuthenticator() throws Exception {
		environmentVariables.set("API_username", "env username");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final Exception exception = assertThrows(IllegalStateException.class, () -> QueriesHelper.getAuthenticator());
		assertEquals("password is missing for username env username", exception.getMessage());
	}

	@Test
	public void testPropAndEnvUserNameNoPasswordGetAuthenticator() throws Exception {
		environmentVariables.set("API_username", "env username");
		System.setProperty("API_username", "prop username");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final Exception exception = assertThrows(IllegalStateException.class, () -> QueriesHelper.getAuthenticator());
		assertEquals("password is missing for username prop username", exception.getMessage());
	}
}