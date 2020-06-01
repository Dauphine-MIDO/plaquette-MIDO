package io.github.oliviercailloux.creds_read;

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

public class CredsReader {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CredsReader.class);

	/**
	 * The default value of the username key used in
	 * <code>CredsReader.defaultCreds()</code>.
	 */
	public static final String DEFAULT_USERNAME_KEY = "API_username";

	/**
	 * The default value of the password key used in
	 * <code>CredsReader.defaultCreds()</code>.
	 */
	public static final String DEFAULT_PASSWORD_KEY = "API_password";

	/**
	 * The default value of the file path used in
	 * <code>CredsReader.defaultCreds()</code>.
	 */
	public static final Path DEFAULT_FILE_NAME = Path.of("API_login.txt");

	/**
	 * The value of the username key. It can be set in <code>given(String
	 * usernameKey, String passwordKey, String filePath)
	 */
	private final String usernameKey;

	/**
	 * The value of the password key. It can be set in <code>given(String
	 * usernameKey, String passwordKey, String filePath)
	 */
	private final String passwordKey;

	/**
	 * The value of the file path. It can be set in
	 * <code>given(String usernameKey, String passwordKey, Path filePath)</code>.
	 */
	private final Path filePath;

	public final Map<String, String> env = System.getenv();

	public static CredsReader given(String usernameKey, String passwordKey, Path filePath) {
		CredsReader credsReader = new CredsReader(usernameKey, passwordKey, filePath);
		return credsReader;
	}

	public static CredsReader defaultCreds() {
		CredsReader credsReader = new CredsReader(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY, DEFAULT_FILE_NAME);
		return credsReader;
	}

	private CredsReader(String usernameKey, String passwordKey, Path filePath) {
		this.usernameKey = checkNotNull(usernameKey);
		this.passwordKey = checkNotNull(passwordKey);
		this.filePath = checkNotNull(filePath);
	}

	public String getUsernameKey() {
		return usernameKey;
	}

	public String getPasswordKey() {
		return passwordKey;
	}

	public Path getFilePath() {
		return filePath;
	}

	/**
	 * Returns the best login information found, or an exception if some information
	 * is missing.
	 *
	 * @throws IllegalStateException if information is missing
	 */
	public Credentials getCredentials() throws IllegalStateException {
		final CredsOpt credsOpt;
		try {
			credsOpt = readCredentials();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		if (credsOpt.getUsername().isEmpty() && credsOpt.getPassword().isEmpty()) {
			throw new IllegalStateException("Login information not found.");
		}
		if (credsOpt.getUsername().isEmpty()) {
			throw new IllegalStateException("Found password but no username.");
		}
		if (credsOpt.getPassword().isEmpty()) {
			throw new IllegalStateException("Found username but no password.");
		}
		final Credentials credentials = Credentials.given(credsOpt.getUsername().get(), credsOpt.getPassword().get());
		return credentials;
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
	 * {@link CredsOpt#getInformationalValue()} (meaning that sources are ordered by
	 * increasing number of pieces of information missing), and, in case of ex-æquo,
	 * the order of priority displayed in the previous paragraph determines which
	 * source wins.
	 * </p>
	 *
	 * @throws IllegalStateException if a file source is provided but has non empty
	 *                               line content after the second line.
	 * @see CredsOpt
	 */
	private CredsOpt readCredentials() throws IOException, IllegalStateException {
		final CredsOpt propertyAuthentication;
		{
			final String username = System.getProperty(this.usernameKey);
			final String password = System.getProperty(this.passwordKey);
			propertyAuthentication = CredsOpt.given(Optional.ofNullable(username), Optional.ofNullable(password));
			final int informationalValue = propertyAuthentication.getInformationalValue();
			LOGGER.info(
					"Found {} piece" + (informationalValue >= 2 ? "s" : "") + " of login information in properties.",
					informationalValue);
		}

		final CredsOpt envAuthentication;
		{
			final String username = env.get(this.usernameKey);
			final String password = env.get(this.passwordKey);
			envAuthentication = CredsOpt.given(Optional.ofNullable(username), Optional.ofNullable(password));
			final int informationalValue = envAuthentication.getInformationalValue();
			LOGGER.info("Found {} piece" + (informationalValue >= 2 ? "s" : "")
					+ " of login information in environment variables.", informationalValue);
		}

		final CredsOpt fileAuthentication;
		{
			final Optional<String> optUsername;
			final Optional<String> optPassword;
			final Path path = filePath;
			if (!Files.exists(path)) {
				optUsername = Optional.empty();
				optPassword = Optional.empty();
			} else {
				final List<String> lines = Files.readAllLines(path);
				final Iterator<String> iterator = lines.iterator();
				if (iterator.hasNext()) {
					optUsername = Optional.of(iterator.next());
				} else {
					optUsername = Optional.of("");
				}
				if (iterator.hasNext()) {
					optPassword = Optional.of(iterator.next());
				} else {
					optPassword = Optional.of("");
				}
				while (iterator.hasNext()) {
					if (!iterator.next().isEmpty()) {
						throw new IllegalStateException(
								"File " + filePath + " is too long: " + lines.size() + " lines");
					}
				}
			}
			fileAuthentication = CredsOpt.given(optUsername, optPassword);
			final int informationalValue = fileAuthentication.getInformationalValue();
			LOGGER.info("Found {} piece" + (informationalValue >= 2 ? "s" : "") + " of login information in file.",
					informationalValue);
		}

		final TreeMap<Double, CredsOpt> map = new TreeMap<>();
		map.put(propertyAuthentication.getInformationalValue() * 1.2d, propertyAuthentication);
		map.put(envAuthentication.getInformationalValue() * 1.1d, envAuthentication);
		map.put(fileAuthentication.getInformationalValue() * 1.0d, fileAuthentication);
		return map.lastEntry().getValue();
	}

}
