package org.SalimMRP.business.auth;

public interface PasswordHasher {

    String hash(String plainText);

    boolean matches(String plainText, String hash);
}
