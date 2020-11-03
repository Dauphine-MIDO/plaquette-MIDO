package io.github.oliviercailloux.plaquette_mido_soap;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import io.github.oliviercailloux.jaris.credentials.Credentials;
import io.github.oliviercailloux.jaris.credentials.CredsReader;

public class QueriesHelper {

	public static void setDefaultAuthenticator() {
		final Authenticator myAuth = getConstantAuthenticator(CredsReader.defaultCreds());
		Authenticator.setDefault(myAuth);
	}

	private static Authenticator getConstantAuthenticator(CredsReader credsReader) {
		Credentials credentials = credsReader.getCredentials();
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
