package io.github.oliviercailloux.plaquette_mido_soap;

import io.github.oliviercailloux.jaris.collections.ImmutableCompleteMap;
import io.github.oliviercailloux.jaris.credentials.CredentialsReader;
import io.github.oliviercailloux.jaris.credentials.CredentialsReader.ClassicalCredentials;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class QueriesHelper {

  public static void setDefaultAuthenticator() {
    final Authenticator myAuth =
        getConstantAuthenticator(CredentialsReader.classicalReader().getCredentials());
    Authenticator.setDefault(myAuth);
  }

  private static Authenticator
      getConstantAuthenticator(ImmutableCompleteMap<ClassicalCredentials, String> credentials) {
    final PasswordAuthentication passwordAuthentication =
        new PasswordAuthentication(credentials.get(ClassicalCredentials.API_USERNAME),
            credentials.get(ClassicalCredentials.API_PASSWORD).toCharArray());
    final Authenticator myAuth = new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return passwordAuthentication;
      }
    };
    return myAuth;
  }
}
