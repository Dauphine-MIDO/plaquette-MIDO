package io.github.oliviercailloux.plaquette_mido_soap;

import java.util.Optional;
import com.google.common.base.Preconditions;

/**
 * This class is used to contain the information to connect to the API. It is
 * immutable and can be initialized by an username, a password, both or nothing.
 */
public class Authentication {
	private final Optional<String> userName;
	private final Optional<String> password;

	private Authentication(Optional<String> userName, Optional<String> password) {
		this.userName = Preconditions.checkNotNull(userName);
		this.password = password;
	}

	public static Authentication given(Optional<String> userName, Optional<String> password) {
		Authentication authentication = new Authentication(userName, password);
		return authentication;
	}

	public static Authentication given(String tokenUserName, String tokenPassword) {
		Authentication authentication = new Authentication(Optional.of(tokenUserName), Optional.of(tokenPassword));
		return authentication;
	}

	public static Authentication onlyUserName(String token) {
		Authentication authentication = new Authentication(Optional.of(token), Optional.empty());
		return authentication;
	}

	public static Authentication onlyPassword(String token) {
		Authentication authentication = new Authentication(Optional.empty(), Optional.of(token));
		return authentication;
	}

	public static Authentication empty() {
		Authentication authentication = new Authentication(Optional.empty(), Optional.empty());
		return authentication;
	}

	public Optional<String> getUserName() {
		return userName;
	}

	public Optional<String> getPassword() {
		return password;
	}
}
