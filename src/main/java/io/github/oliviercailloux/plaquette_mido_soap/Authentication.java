package io.github.oliviercailloux.plaquette_mido_soap;

import java.util.Optional;

/**
 * This class is used to contain the information to connect to the API. It is
 * immutable and can be initialized by an username, a password, both or nothing.
 */
public class Authentication {
	private final Optional<String> userName;
	private final Optional<String> password;

	/**
	 * Crée une classe Authentication (visibilité package) avec des champs final
	 * Optional<String> userName et password. Le constructeur les initialise à
	 * Optional.empty(). Indique dans la javadoc de la classe qu’elle est immuable.
	 * Il y a (donc) des getter et pas de setter. La classe a un constructeur privé
	 * (Optional<String>, Optional<String>), une static factory method given(String
	 * userName, String password), une static factory method onlyUserName(String),
	 * une static factory method onlyPassword(String) et une static factory method
	 * empty().
	 * 
	 * Change la méthode getTokenOpt() pour qu’elle renvoie un Authentication, et
	 * renomme-la d’ailleurs readAuthentication(). Change getTokenValue() en
	 * getAuthentication() qui renvoie un PasswordAuthentication ou lance une
	 * exception informative si le password manque : l’exception doit indiquer si un
	 * login a été trouvé, et si oui, quel login (cette information n’est pas
	 * considérée comme secrète). Cette méthode utilise le login par défaut si aucun
	 * n’a été lu. Il faut d’ailleurs stocker le login par défaut dans une variable
	 * statique publique de la classe QueriesHelper.
	 * 
	 */

	private Authentication(Optional<String> userName, Optional<String> password) {
		this.userName = userName;
		this.password = password;
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
