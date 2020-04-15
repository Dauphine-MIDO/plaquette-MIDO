package io.github.oliviercailloux.plaquette_mido_soap;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

class AuthenticationTest {

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();


	@Test
	public void testPropReadAuthentication() throws IOException {

		System.setProperty("API_username", "prop username");
		System.setProperty("API_password", "prop password");
		Authentication myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUserName().get());
		assertEquals("prop password", myAuth.getPassword().get());

		System.clearProperty("API_username");
		System.clearProperty("API_password");
	}

	@Test
	public void testReadAuthentication() throws IOException {

		environmentVariables.set("API_username", "env username");
		System.setProperty("API_username", "prop username");
		System.setProperty("API_password", "prop password");
		Authentication myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUserName().get());
		assertEquals("prop password", myAuth.getPassword().get());

		environmentVariables.set("API_username", null);
		System.clearProperty("API_username");
		System.clearProperty("API_password");
	}

	@Test
	public void testEnvReadAuthentication() throws IOException {

		environmentVariables.set("API_username", "env username");
		environmentVariables.set("API_password", "env password");
		System.setProperty("API_username", "prop username");
		Authentication myAuth = QueriesHelper.readAuthentication();

		assertEquals("env username", myAuth.getUserName().get());
		assertEquals("env password", myAuth.getPassword().get());

		environmentVariables.set("API_username", null);
		environmentVariables.set("API_password", null);
		System.clearProperty("API_username");
	}

	@Test
	public void testEmptypasswordReadAuthentication() throws IOException {
		environmentVariables.set("API_username", "env username");
		System.setProperty("API_username", "prop username");
		Authentication myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUserName().get());

		environmentVariables.set("API_username", null);
		System.clearProperty("API_username");
	}

	@Test
	public void testEnvOrPropAuthentication() throws IOException {

		environmentVariables.set("API_username", "env username");
		environmentVariables.set("API_password", "env password");
		System.setProperty("API_username", "prop username");
		System.setProperty("API_password", "prop password");
		Authentication myAuth = QueriesHelper.readAuthentication();

		assertEquals("prop username", myAuth.getUserName().get());
		assertEquals("prop password", myAuth.getPassword().get());

		environmentVariables.set("API_username", null);
		environmentVariables.set("API_password", null);
		System.clearProperty("API_username");
		System.clearProperty("API_password");
	}


	@Test
	public void testNoneGetTokenAuthenticator() {
		Exception exception = assertThrows(IllegalStateException.class, () -> QueriesHelper.getTokenAuthenticator());
		assertEquals("username is missing", exception.getMessage());
	}
	
	@Test
	public void testEnvUserNameGetTokenAuthenticator() {
		environmentVariables.set("API_username", "env username");
		
		Exception exception = assertThrows(IllegalStateException.class, () -> QueriesHelper.getTokenAuthenticator());
		assertEquals("password is missing for username env username", exception.getMessage());
		
		environmentVariables.set("API_username", null);
	}
	@Test
	public void testPropUserNameGetTokenAuthenticator() {
		environmentVariables.set("API_username", "env username");
		System.setProperty("API_username", "prop username");
		
		Exception exception = assertThrows(IllegalStateException.class, () -> QueriesHelper.getTokenAuthenticator());
		assertEquals("password is missing for username prop username", exception.getMessage());
		
		environmentVariables.set("API_username", null);
		System.clearProperty("API_username");
	}
}