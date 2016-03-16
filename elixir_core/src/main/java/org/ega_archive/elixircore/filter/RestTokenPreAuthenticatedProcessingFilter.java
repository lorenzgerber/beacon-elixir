package org.ega_archive.elixircore.filter;

import org.ega_archive.elixircore.constant.CoreConstants;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

@Log4j
public class RestTokenPreAuthenticatedProcessingFilter extends
                                                       AbstractPreAuthenticatedProcessingFilter {

  private final String TOKEN_HEADER = CoreConstants.TOKEN_HEADER;

  @Override
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
    String token = request.getHeader(TOKEN_HEADER);
    if (token == null) { //In this case, we do not found a token, so anonymous user is used
      return null;
    }
    return token;
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return "N/A";
  }

  @Override
  protected void unsuccessfulAuthentication(javax.servlet.http.HttpServletRequest request,
                                            javax.servlet.http.HttpServletResponse response,
                                            AuthenticationException failed) {
    super.unsuccessfulAuthentication(request, response, failed);
    try {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, failed.getMessage());
    } catch (IOException e) {
      logger.error(e);
    }
  }
}
