package io.github.oliviercailloux.plaquette_mido_soap;

import java.net.Authenticator;

import io.github.oliviercailloux.creds_read.Credentials;
import io.github.oliviercailloux.creds_read.CredsReader;

public class QueriesHelper {

	public static void setDefaultAuthenticator() {
		CredsReader credsReader = CredsReader.defaultCreds();
		final Credentials credentials = credsReader.getCredentials();

		final Authenticator myAuth = CredsReader.getConstantAuthenticator(credentials);
		Authenticator.setDefault(myAuth);
	}
}
