package io.github.oliviercailloux.plaquette_mido_soap;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

/**
 * Immutable.
 */
class LoginOpt {
	public static LoginOpt given(Optional<String> userName, Optional<String> password) {
		LoginOpt authentication = new LoginOpt(userName, password);
		return authentication;
	}

	public static LoginOpt given(String username, String password) {
		LoginOpt authentication = new LoginOpt(Optional.of(username), Optional.of(password));
		return authentication;
	}

	public static LoginOpt onlyUsername(String username) {
		LoginOpt authentication = new LoginOpt(Optional.of(username), Optional.empty());
		return authentication;
	}

	public static LoginOpt onlyPassword(String password) {
		LoginOpt authentication = new LoginOpt(Optional.empty(), Optional.of(password));
		return authentication;
	}

	public static LoginOpt empty() {
		LoginOpt authentication = new LoginOpt(Optional.empty(), Optional.empty());
		return authentication;
	}

	private final Optional<String> username;
	private final Optional<String> password;

	private LoginOpt(Optional<String> username, Optional<String> password) {
		this.username = checkNotNull(username);
		this.password = checkNotNull(password);
	}

	public Optional<String> getUsername() {
		return username;
	}

	public Optional<String> getPassword() {
		return password;
	}

	public int getInformationValue() {
		return (username.isPresent() ? 1 : 0) + (password.isPresent() ? 1 : 0);
	}
}
