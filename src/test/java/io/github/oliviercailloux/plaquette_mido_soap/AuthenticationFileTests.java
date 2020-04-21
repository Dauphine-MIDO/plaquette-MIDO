package io.github.oliviercailloux.plaquette_mido_soap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

class AuthenticationFileTests {

	private static Path createApiLoginFile(List<String> lines) throws IOException {
		FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
		Path filePath = fileSystem.getPath("API_login.txt");
		Files.write(filePath, lines);
		return filePath;
	}

	@Test
	public void testFileReadAuthentication() throws IOException {
		ImmutableList<String> lines = ImmutableList.of("file username", "file password");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@Test
	public void testEmptyUsernameFileReadAuthentication() throws IOException {
		List<String> lines = Arrays.asList("", "file password");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@Test
	public void testNoPasswordFileReadAuthentication() throws IOException {
		List<String> lines = Arrays.asList("file username");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertTrue(myAuth.getPassword().isEmpty());
	}

	@Test
	public void testEmptyPasswordFileReadAuthentication() throws IOException {
		List<String> lines = Arrays.asList("file username", "");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("", myAuth.getPassword().get());
	}

	@Test
	public void testIncorrectFileReadAuthentication() throws IOException {
		List<String> lines = Arrays.asList("file username", "file password", "garbage");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);

		final Exception exception = assertThrows(IllegalStateException.class, () -> QueriesHelper.readAuthentication());
		assertEquals("File API_login.txt is too long: 3 lines", exception.getMessage());
	}

	@Test
	public void testEmptyThirdLineFileReadAuthentication() throws IOException {
		List<String> lines = Arrays.asList("file username", "file password", "");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@Test
	public void testEmptyUsernameGetAuthenticator() throws Exception {
		List<String> lines = Arrays.asList("\nfile password");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);

		assertDoesNotThrow(() -> QueriesHelper.setDefaultAuthenticator());
	}

	@Test
	public void testNoPasswordFileGetAuthenticator() throws Exception {
		List<String> lines = Arrays.asList("file username");
		QueriesHelper.apiLoginFile = createApiLoginFile(lines);

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Found username 'file username' but no password.", exception.getMessage());
	}

}
