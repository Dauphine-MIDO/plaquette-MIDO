package io.github.oliviercailloux.plaquette_mido_soap;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;;

/**
 * This class is used to contain the information to connect to the API. It is
 * immutable and can be initialized by an username, a password, both or nothing.
 */
public class Authentication {
	private final Optional<String> username;
	private final Optional<String> password;

	private Authentication(Optional<String> username, Optional<String> password) {
		this.username = checkNotNull(username);
		this.password = checkNotNull(password);
	}

	public static Authentication given(Optional<String> userName, Optional<String> password) {
		Authentication authentication = new Authentication(userName, password);
		return authentication;
	}

	public static Authentication given(String username, String password) {
		Authentication authentication = new Authentication(Optional.of(username), Optional.of(password));
		return authentication;
	}

	public static Authentication onlyUsername(String username) {
		Authentication authentication = new Authentication(Optional.of(username), Optional.empty());
		return authentication;
	}

	public static Authentication onlyPassword(String password) {
		Authentication authentication = new Authentication(Optional.empty(), Optional.of(password));
		return authentication;
	}

	public static Authentication empty() {
		Authentication authentication = new Authentication(Optional.empty(), Optional.empty());
		return authentication;
	}

	public Optional<String> getUsername() {
		return username;
	}

	public Optional<String> getPassword() {
		return password;
	}
}
