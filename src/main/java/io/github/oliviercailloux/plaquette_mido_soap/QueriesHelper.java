package io.github.oliviercailloux.plaquette_mido_soap;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class QueriesHelper {

	private static String userName;
	
	public static void setDefaultAuthenticator() {
		final Authenticator myAuth = getTokenAuthenticator();
		Authenticator.setDefault(myAuth);
	}

	public static Authenticator getTokenAuthenticator() {
		final String tokenValue;
		try {
			tokenValue = getTokenValue();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		if (userName.isEmpty())
			userName="plaquette-mido";
		final PasswordAuthentication passwordAuthentication = new PasswordAuthentication(userName,
				tokenValue.toCharArray());
		final Authenticator myAuth = getConstantAuthenticator(passwordAuthentication);
		return myAuth;
	}

	private static String getTokenValue() throws IOException, IllegalStateException {
		final Optional<String> tokenOpt = getTokenOpt();
		return tokenOpt
				.orElseThrow(() -> new IllegalStateException("No token found in environment, in property or in file."));
	}

	private static Optional<String> getTokenOpt() throws IOException {
		{
			final String token = System.getenv("API_password");
			if (token != null) {
				return Optional.of(token);
			}
		}
		{
			final String token = System.getProperty("API_password");
			if (token != null) {
				return Optional.of(token);
			}
		}
		final Path path = Paths.get("API_password.txt");
		if (!Files.exists(path)) {
			return Optional.empty();
		}
		final List<String> lines = new ArrayList<String>(Files.readAllLines(path, StandardCharsets.UTF_8));
		userName=lines.get(0);
		final String content = lines.get(1);
		return Optional.of(content.replaceAll("\n", ""));
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
