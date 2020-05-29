package io.github.oliviercailloux.plaquette_mido_soap;

import java.net.Authenticator;

import io.github.oliviercailloux.creds_read.Credentials;
import io.github.oliviercailloux.creds_read.CredsReader;

public class QueriesHelper {

	public static void setDefaultAuthenticator() {
		final Authenticator myAuth = CredsReader.defaultCreds().getConstantAuthenticator();
		Authenticator.setDefault(myAuth);
	}
}
