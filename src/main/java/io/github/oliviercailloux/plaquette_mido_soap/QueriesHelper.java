package io.github.oliviercailloux.plaquette_mido_soap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

public class QueriesHelper {

	public static void setDefaultAuthenticator() {
		final Authenticator myAuth = getTokenAuthenticator();
		Authenticator.setDefault(myAuth);
	}

	public static Authenticator getTokenAuthenticator() {
		final PasswordAuthentication passwordAuthentication;
		try {
			passwordAuthentication = getAuthentication();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		final Authenticator myAuth = getConstantAuthenticator(passwordAuthentication);
		return myAuth;
	}

	private static PasswordAuthentication getAuthentication() throws IOException {
		final Authentication authentication = readAuthentication();
		final PasswordAuthentication passwordAuthentication;
		if (authentication.getUsername().isEmpty()) {
			throw new IllegalStateException("username is missing");
		}
		if (authentication.getPassword().isEmpty()) {
			throw new IllegalStateException("password is missing for username " + authentication.getUsername().get());
		}
		passwordAuthentication = new PasswordAuthentication(authentication.getUsername().get(),
				authentication.getPassword().get().toCharArray());
		return passwordAuthentication;
	}

	static Authentication readAuthentication() throws IOException {

		TreeMap<Float, Authentication> map = new TreeMap<>();
		Optional<String> optUserName;
		Optional<String> optPassword;

		{
			final String tokenUserName = System.getProperty("API_username");
			final String tokenPassword = System.getProperty("API_password");
			if (tokenUserName != null) {
				optUserName = Optional.of(tokenUserName);

				if (tokenPassword != null) {
					optPassword = Optional.of(tokenPassword);
					map.put((float) 1.3, Authentication.given(optUserName, optPassword));
				} else {
					optPassword = Optional.ofNullable(tokenPassword);
					map.put((float) .3, Authentication.given(optUserName, optPassword));
				}
			}
		}

		{
			final String tokenUserName = System.getenv("API_username");
			final String tokenPassword = System.getenv("API_password");
			if (tokenUserName != null) {
				optUserName = Optional.of(tokenUserName);

				if (tokenPassword != null) {
					optPassword = Optional.of(tokenPassword);
					map.put((float) 1.2, Authentication.given(optUserName, optPassword));
				} else {
					optPassword = Optional.ofNullable(tokenPassword);
					map.put((float) .2, Authentication.given(optUserName, optPassword));
				}
			}
		}

		{
			final Path path = Paths.get("API_login.txt");
			if (!Files.exists(path)) {
				map.put((float) 0, Authentication.empty());
			}
			final List<String> lines = new ArrayList<String>(Files.readAllLines(path, StandardCharsets.UTF_8));
			{
				if (lines.isEmpty()) {
					map.put((float) 0, Authentication.empty());
				} else if (lines.size() == 1) {
					optUserName = Optional.of(lines.get(0).replaceAll("\n", ""));
					optPassword = Optional.empty();
					map.put((float) .1, Authentication.given(optUserName, optPassword));
				} else if (lines.size() == 2) {
					optUserName = Optional.of(lines.get(0).replaceAll("\n", ""));
					optPassword = Optional.of(lines.get(1).replaceAll("\n", ""));
					map.put((float) 1.1, Authentication.given(optUserName, optPassword));
				} else {
					throw new IllegalStateException(lines.toString() + " File API_login.txt is not written correctly");
				}
			}
		}

		return map.lastEntry().getValue();

	}

	private static Authenticator getConstantAuthenticator(PasswordAuthentication passwordAuthentication) {
		final Authenticator myAuth = new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return passwordAuthentication;
			}
		};
		return myAuth;
	}

}
