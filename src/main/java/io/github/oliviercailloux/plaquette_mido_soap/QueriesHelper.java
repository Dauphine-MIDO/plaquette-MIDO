package io.github.oliviercailloux.plaquette_mido_soap;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

public class QueriesHelper {
	static Path apiLoginFile = Path.of("API_login.txt");

	public static void setDefaultAuthenticator() {
		final Authenticator myAuth = getAuthenticator();
		Authenticator.setDefault(myAuth);
	}

	/**
	 * Retrives the best login information that can be found, or an exception if
	 * some information is missing.
	 *
	 * @throws IllegalStateException if information is missing
	 */
	private static Authenticator getAuthenticator() throws IllegalStateException {
		final PasswordAuthentication passwordAuthentication = getAuthentication();

		return getConstantAuthenticator(passwordAuthentication);
	}

	/**
	 * Returns the best login information found, or an exception if some information
	 * is missing.
	 *
	 * @throws IllegalStateException if information is missing
	 */
	private static PasswordAuthentication getAuthentication() throws IllegalStateException {
		final LoginOpt authentication;
		try {
			authentication = readAuthentication();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		if (authentication.getUsername().isEmpty() && authentication.getPassword().isEmpty()) {
			throw new IllegalStateException("Login information not found.");
		}
		if (authentication.getUsername().isEmpty()) {
			throw new IllegalStateException("Found password but no username.");
		}
		if (authentication.getPassword().isEmpty()) {
			throw new IllegalStateException(
					"Found username '" + authentication.getUsername().get() + "' but no password.");
		}
		final PasswordAuthentication passwordAuthentication = new PasswordAuthentication(
				authentication.getUsername().get(), authentication.getPassword().get().toCharArray());
		return passwordAuthentication;
	}

	/**
	 * Returns the best authentication information it could find, throwing no error
	 * if some is missing.
	 */
	static LoginOpt readAuthentication() throws IOException {
		final LoginOpt propertyAuthentication;
		{
			final String username = System.getProperty("API_username");
			final String password = System.getProperty("API_password");
			propertyAuthentication = LoginOpt.given(Optional.ofNullable(username), Optional.ofNullable(password));
		}

		final LoginOpt envAuthentication;
		{
			final String username = System.getenv("API_username");
			final String password = System.getenv("API_password");
			envAuthentication = LoginOpt.given(Optional.ofNullable(username), Optional.ofNullable(password));
		}

		final LoginOpt fileAuthentication;
		{
			final Optional<String> optUsername;
			final Optional<String> optPassword;
			final Path path = apiLoginFile;
			if (!Files.exists(path)) {
				optUsername = Optional.empty();
				optPassword = Optional.empty();
			} else {
				final List<String> lines = Files.readAllLines(path);
				final Iterator<String> iterator = lines.iterator();
				if (iterator.hasNext()) {
					optUsername = Optional.of(iterator.next());
				} else {
					optUsername = Optional.empty();
				}
				if (iterator.hasNext()) {
					optPassword = Optional.of(iterator.next());
				} else {
					optPassword = Optional.empty();
				}
				if (iterator.hasNext()) {
					throw new IllegalStateException(
							"File " + apiLoginFile + " is too long: " + lines.size() + " lines");
				}
			}
			fileAuthentication = LoginOpt.given(optUsername, optPassword);
		}

		final TreeMap<Double, LoginOpt> map = new TreeMap<>();
		map.put(propertyAuthentication.getInformationValue() * 1.2d, propertyAuthentication);
		map.put(envAuthentication.getInformationValue() * 1.1d, envAuthentication);
		map.put(fileAuthentication.getInformationValue() * 1.0d, fileAuthentication);
		return map.lastEntry().getValue();
	}

	private static Authenticator getConstantAuthenticator(PasswordAuthentication passwordAuthentication) {
		checkNotNull(passwordAuthentication);
		final Authenticator myAuth = new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return passwordAuthentication;
			}
		};
		return myAuth;
	}

}
