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

	private final String USERNAME_KEY;

	private final String PASSWORD_KEY;

	private final String FILE_NAME;

	private static final String DEFAULT_USERNAME_KEY = "API_username";

	private static final String DEFAULT_PASSWORD_KEY = "API_password";

	private static final String DEFAULT_FILE_NAME = "API_login.txt";

	private static final String defaultUsernameKey = "API_username";

	private static final String defaultPasswordKey = "API_password";

	private static final String defaultFileName = "API_login.txt";

	static Map<String, String> env = System.getenv();

	static Path credsFile;

	public static CredsReader given(String USERNAME_KEY, String PASSWORD_KEY, String FILE_NAME) {
		CredsReader credsReader = new CredsReader(USERNAME_KEY, PASSWORD_KEY, FILE_NAME);
		return credsReader;
	}

	public static CredsReader defaultCreds() {
		CredsReader credsReader = new CredsReader(DEFAULT_USERNAME_KEY, DEFAULT_PASSWORD_KEY, DEFAULT_FILE_NAME);
		return credsReader;
	}

	private CredsReader(String USERNAME_KEY, String PASSWORD_KEY, String FILE_NAME) {
		this.USERNAME_KEY = checkNotNull(USERNAME_KEY);
		this.PASSWORD_KEY = checkNotNull(PASSWORD_KEY);
		this.FILE_NAME = checkNotNull(FILE_NAME);
		credsFile = Path.of(FILE_NAME);
	}

	public String getUsernameKey() {
		return USERNAME_KEY;
	}

	public String getPasswordKey() {
		return PASSWORD_KEY;
	}

	public String getFileName() {
		return FILE_NAME;
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
	public CredsOpt readCredentials() throws IOException, IllegalStateException {
		final CredsOpt propertyAuthentication;
		{
			final String username = System.getProperty(this.USERNAME_KEY);
			final String password = System.getProperty(this.PASSWORD_KEY);
			propertyAuthentication = CredsOpt.given(Optional.ofNullable(username), Optional.ofNullable(password));
			final int informationalValue = propertyAuthentication.getInformationalValue();
			LOGGER.info(
					"Found {} piece" + (informationalValue >= 2 ? "s" : "") + " of login information in properties.",
					informationalValue);
		}

		final CredsOpt envAuthentication;
		{
			final String username = env.get(this.USERNAME_KEY);
			final String password = env.get(this.PASSWORD_KEY);
			envAuthentication = CredsOpt.given(Optional.ofNullable(username), Optional.ofNullable(password));
			final int informationalValue = envAuthentication.getInformationalValue();
			LOGGER.info("Found {} piece" + (informationalValue >= 2 ? "s" : "")
					+ " of login information in environment variables.", informationalValue);
		}

		final CredsOpt fileAuthentication;
		{
			final Optional<String> optUsername;
			final Optional<String> optPassword;
			final Path path = credsFile;
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
								"File " + credsFile + " is too long: " + lines.size() + " lines");
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

	public static Authenticator getConstantAuthenticator(Credentials credentials) {
		checkNotNull(credentials);
		final PasswordAuthentication passwordAuthentication = new PasswordAuthentication(credentials.getUsername(),
				credentials.getPassword().toCharArray());
		final Authenticator myAuth = new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return passwordAuthentication;
			}
		};
		return myAuth;
	}

}
