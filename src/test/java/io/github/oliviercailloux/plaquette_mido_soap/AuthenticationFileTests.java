package io.github.oliviercailloux.plaquette_mido_soap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

class AuthenticationFileTests {

	private static Path writeAPIFile(List<String> lines) {
		FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
		String fileName = "API_login.txt";
		Path pathToStore = fileSystem.getPath("");
		Path filePath = pathToStore.resolve(fileName);
		if (!Files.exists(pathToStore.resolve(fileName))) {
			try {
				Files.createFile(filePath);
			} catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		}
		try {
			Files.write(filePath, lines);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return filePath;
	}

	@Test
	public void testFileReadAuthentication() throws IOException {
		List<String> lines = Arrays.asList("file username", "file password");
		QueriesHelper.apiLoginFile = writeAPIFile(lines);

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@Test
	public void testNoUsernameFileReadAuthentication() throws IOException {
		List<String> lines = Arrays.asList("\nfile password");
		QueriesHelper.apiLoginFile = writeAPIFile(lines);

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertTrue(myAuth.getUsername().isEmpty());
		assertEquals("file password", myAuth.getPassword().get());
	}

	@Test
	public void testNoPasswordFileReadAuthentication() throws IOException {
		List<String> lines = Arrays.asList("file username");
		QueriesHelper.apiLoginFile = writeAPIFile(lines);

		final LoginOpt myAuth = QueriesHelper.readAuthentication();

		assertEquals("file username", myAuth.getUsername().get());
		assertTrue(myAuth.getPassword().isEmpty());
	}

	@Test
	public void testIncorrectFileReadAuthentication() throws IOException {
		List<String> lines = Arrays.asList("file username", "file password", "garbage");
		QueriesHelper.apiLoginFile = writeAPIFile(lines);

		final Exception exception = assertThrows(IllegalStateException.class, () -> QueriesHelper.readAuthentication());
		assertEquals("File API_login.txt is too long: 3 lines", exception.getMessage());
	}

	@Test
	public void testNoUsernameGetAuthenticator() throws Exception {
		List<String> lines = Arrays.asList("\nfile password");
		QueriesHelper.apiLoginFile = writeAPIFile(lines);

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Login information not found.", exception.getMessage());
	}

	@Test
	public void testNoPasswordFileGetAuthenticator() throws Exception {
		List<String> lines = Arrays.asList("file username");
		QueriesHelper.apiLoginFile = writeAPIFile(lines);

		final Exception exception = assertThrows(IllegalStateException.class,
				() -> QueriesHelper.setDefaultAuthenticator());
		assertEquals("Found username 'file username' but no password.", exception.getMessage());
	}

}
