package tech.simter.rest.jaxrs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import tech.simter.Context;
import tech.simter.jwt.DecodeException;
import tech.simter.jwt.JWT;

import javax.annotation.Priority;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Verify the JWT Authorization info, then set all payload data into {@link ThreadLocal} variable.
 * <p>
 * App can get the variable value through {@link Context#get(String)} method.
 * Throws {@link DecodeException} If JWT verify failed.
 *
 * @author RJ
 */
@Named
@Singleton
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthRequestFilter implements ContainerRequestFilter {
  private static Logger logger = LoggerFactory.getLogger(JwtAuthRequestFilter.class);
  /**
   * The header name to hold JWT token
   */
  public static final String JWT_HEADER_NAME = "Authorization";
  private final String secretKey;
  private final boolean abortIfUnauthorized;

  /**
   * @param secretKey           the secret-key
   * @param abortIfUnauthorized true to abort request if the JWT verified failed
   */
  public JwtAuthRequestFilter(@Value("${simter.jwt.secret-key:}") String secretKey,
                              @Value("${simter.jwt.abort-if-unauthorized:false}") boolean abortIfUnauthorized) {
    this.secretKey = secretKey;
    this.abortIfUnauthorized = abortIfUnauthorized;
  }

  @Override
  public void filter(ContainerRequestContext c) throws IOException {
    String authorization = c.getHeaderString(JWT_HEADER_NAME);
    if (authorization == null || authorization.isEmpty() || !authorization.startsWith("Bearer ")) {
      if (abortIfUnauthorized) abortWithForbidden(c, "No valid JWT header");
    } else {
      try {
        // verify and decode to a JWT instance
        authorization = authorization.substring(7);
        logger.debug("jwt={}", authorization);
        JWT jwt = JWT.verify(authorization, secretKey);  // "Bearer ".length() == 7;

        // set all payload data to context
        jwt.payload.getData().forEach(Context::set);
      } catch (DecodeException e) {
        if (logger.isDebugEnabled()) logger.debug(e.getMessage(), e);
        else logger.warn(e.getMessage());
        if (abortIfUnauthorized) abortWithForbidden(c, "Invalid JWT");
      }
    }
  }

  private void abortWithForbidden(ContainerRequestContext c, String msg) {
    c.abortWith(Response.status(Response.Status.FORBIDDEN)
      .type(MediaType.TEXT_PLAIN)
      .entity(msg)
      .build()
    );
  }
}