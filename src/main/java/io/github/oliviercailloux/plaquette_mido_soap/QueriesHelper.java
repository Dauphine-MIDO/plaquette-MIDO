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
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueriesHelper {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(QueriesHelper.class);

	public static final String USERNAME_KEY = "API_username";

	public static final String PASSWORD_KEY = "API_password";

	public static final String FILE_NAME = "API_login.txt";
	static Map<String, String> env = System.getenv();

	static Path apiLoginFile = Path.of(FILE_NAME);

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
			throw new IllegalStateException("Found username but no password.");
		}
		final PasswordAuthentication passwordAuthentication = new PasswordAuthentication(
				authentication.getUsername().get(), authentication.getPassword().get().toCharArray());
		return passwordAuthentication;
	}

	/**
	 * <p>
	 * Returns the best authentication information it could find, throwing no error
	 * if some is missing.
	 * </p>
	 * <p>
	 * For each piece of information, distinguishes <em>missing information</em> and
	 * <em>empty string</em>. Considers the following possible sources (displayed
	 * here by order of priority).
	 * </p>
	 * <ol>
	 * <li>Properties {@value #USERNAME_KEY} and {@value #PASSWORD_KEY}. Each
	 * property may be set, including to the empty string, or not set. An
	 * information is considered missing (from the properties source) iff the
	 * corresponding property is not set.</li>
	 * <li>Environment variables {@value #USERNAME_KEY} and {@value #PASSWORD_KEY}.
	 * Each variable may be set, including to the empty string, or not set. An
	 * information is considered missing (from the environment variables source) iff
	 * the corresponding environment variable is not set.</li>
	 * <li>File {@value #FILE_NAME}. The two pieces of information are considered
	 * missing (from the files source) iff the file does not exist. If the file
	 * exists, no piece of information is considered missing. The first line of the
	 * file gives the username, the second one gives the password. If the file has
	 * only one line, the password (from the files source) is set to the empty
	 * string. If the file is empty, both pieces of information (from the files
	 * source) are set to the empty string. Empty lines are not considered at all.
	 * If the file has non empty line content after the second line, it is an
	 * error.</li>
	 * </ol>
	 * <p>
	 * The source used to return information is the one that has the highest
	 * informational value, as determined by
	 * {@link LoginOpt#getInformationalValue()} (meaning that sources are ordered by
	 * increasing number of pieces of information missing), and, in case of ex-æquo,
	 * the order of priority displayed in the previous paragraph determines which
	 * source wins.
	 * </p>
	 *
	 * @throws IllegalStateException if a file source is provided but has non empty
	 *                               line content after the second line.
	 * @see LoginOpt
	 */
	static LoginOpt readAuthentication() throws IOException, IllegalStateException {
		final LoginOpt propertyAuthentication;
		{
			final String username = System.getProperty(USERNAME_KEY);
			final String password = System.getProperty(PASSWORD_KEY);
			propertyAuthentication = LoginOpt.given(Optional.ofNullable(username), Optional.ofNullable(password));
			final int informationalValue = propertyAuthentication.getInformationalValue();
			LOGGER.info(
					"Found {} piece" + (informationalValue >= 2 ? "s" : "") + " of login information in properties.",
					informationalValue);
		}

		final LoginOpt envAuthentication;
		{
			final String username = env.get(USERNAME_KEY);
			final String password = env.get(PASSWORD_KEY);
			envAuthentication = LoginOpt.given(Optional.ofNullable(username), Optional.ofNullable(password));
			final int informationalValue = envAuthentication.getInformationalValue();
			LOGGER.info("Found {} piece" + (informationalValue >= 2 ? "s" : "")
					+ " of login information in environment variables.", informationalValue);
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
					String line1 = iterator.next();
					optUsername = Optional.of(line1);
				} else {
					optUsername = Optional.of("");
				}
				if (iterator.hasNext()) {
					String line2 = iterator.next();
					optPassword = Optional.of(line2);
				} else {
					optPassword = Optional.of("");
				}
				while (iterator.hasNext()) {
					if (!iterator.next().isEmpty()) {
						throw new IllegalStateException(
								"File " + apiLoginFile + " is too long: " + lines.size() + " lines");
					}
				}
			}
			fileAuthentication = LoginOpt.given(optUsername, optPassword);
			final int informationalValue = fileAuthentication.getInformationalValue();
			LOGGER.info("Found {} piece" + (informationalValue >= 2 ? "s" : "") + " of login information in file.",
					informationalValue);
		}

		final TreeMap<Double, LoginOpt> map = new TreeMap<>();
		map.put(propertyAuthentication.getInformationalValue() * 1.2d, propertyAuthentication);
		map.put(envAuthentication.getInformationalValue() * 1.1d, envAuthentication);
		map.put(fileAuthentication.getInformationalValue() * 1.0d, fileAuthentication);
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
