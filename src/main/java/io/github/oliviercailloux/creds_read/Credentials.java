package io.github.oliviercailloux.creds_read;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

/**
 * <p>
 * Immutable.
 * </p>
 * <p>
 * Stores two String of login information: username and password.
 * </p>
 */
public class Credentials {

	public static Credentials given(String username, String password) {
		Credentials authentication = new Credentials(username, password);
		return authentication;
	}

	private final String username;
	private final String password;

	private Credentials(String username, String password) {
		this.username = checkNotNull(username);
		this.password = checkNotNull(password);
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}
