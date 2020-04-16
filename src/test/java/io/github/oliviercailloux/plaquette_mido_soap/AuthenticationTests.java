package io.github.oliviercailloux.plaquette_mido_soap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AuthenticationTests {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTests.class);

	/**
	 * This is for JUnit 4, no equivalent system for JUnit 5 found (but see
	 * https://github.com/stefanbirkner/system-rules/issues/55). Apparently, it
	 * works…
	 */
	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

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
		environmentVariables.set("API_username", "env username");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUsername().get());
		assertEquals("prop password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@Test
	public void testHalfPropAndEnvReadAuthentication() throws Exception {
		environmentVariables.set("API_username", "env username");
		environmentVariables.set("API_password", "env password");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("env username", myAuth.getUsername().get());
		assertEquals("env password", myAuth.getPassword().get());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@Test
	public void testNoPasswordReadAuthentication() throws Exception {
		environmentVariables.set("API_username", "env username");
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
		assertEquals("Found username 'env username' but no password.", exception.getMessage());
	}

	@SetSystemProperty(key = "API_username", value = "prop username")
	@Test
	public void testPropAndEnvUserNameNoPasswordGetAuthenticator() throws Exception {
		environmentVariables.set("API_username", "env username");
		QueriesHelper.apiLoginFile = Path.of("nonexistent.txt");

		final Exception exception = assertThrows(IllegalStateException.class, () -> QueriesHelper.getAuthenticator());
		assertEquals("Found username 'prop username' but no password.", exception.getMessage());
	}
}