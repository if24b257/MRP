package org.SalimMRP.business.auth;

public interface TokenService {

    String issueToken(String username);

    boolean isValid(String token);

    String resolveUsername(String token);

    void invalidate(String token);
}
