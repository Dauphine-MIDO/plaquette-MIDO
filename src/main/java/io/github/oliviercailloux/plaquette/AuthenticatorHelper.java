package io.github.oliviercailloux.plaquette;

import io.github.oliviercailloux.jaris.credentials.Credentials;
import io.github.oliviercailloux.jaris.credentials.CredentialsReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class AuthenticatorHelper {

  public static void setDefaultAuthenticator() {
    final Authenticator myAuth =
        getConstantAuthenticator(CredentialsReader.classicalReader().getCredentials());
    Authenticator.setDefault(myAuth);
  }

  private static Authenticator getConstantAuthenticator(Credentials credentials) {
    final PasswordAuthentication passwordAuthentication = new PasswordAuthentication(
        credentials.API_USERNAME(), credentials.API_PASSWORD().toCharArray());
    final Authenticator myAuth = new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return passwordAuthentication;
      }
    };
    return myAuth;
  }
}
