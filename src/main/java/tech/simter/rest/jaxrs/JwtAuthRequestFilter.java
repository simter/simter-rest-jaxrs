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
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import java.util.List;

/**
 * Verify the JWT Authorization info, then set all payload data into {@link ThreadLocal} variable.
 * <p>
 * App can get the variable value through {@link Context#get(String)} method.
 * <p>
 * Abort with {@link Status#UNAUTHORIZED} if without a jwt type `Authorization` header.
 * Throws {@link DecodeException} If JWT header verified failed.
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
  /**
   * The prefix of the jwt header value
   */
  public static final String JWT_VALUE_PREFIX = "Bearer ";
  private String secretKey;
  private boolean requireAuthorized;
  private List<String> excludePaths;
  private boolean hasExcludePaths;

  /**
   * @param secretKey         the secret-key
   * @param requireAuthorized true to abort request if the JWT verified failed
   */
  public JwtAuthRequestFilter(
    @Value("${simter.jwt.secret-key:test}") String secretKey,
    @Value("${simter.jwt.require-authorized:false}") boolean requireAuthorized,
    @Value("${simter.jwt.exclude-paths:#{null}}") List<String> excludePaths
  ) {
    this.secretKey = secretKey;
    this.requireAuthorized = requireAuthorized;
    this.excludePaths = excludePaths;
    this.hasExcludePaths = excludePaths != null && !excludePaths.isEmpty();
  }

  @Override
  public void filter(ContainerRequestContext c) {
    if (!requireAuthorized
      || HttpMethod.OPTIONS.equals(c.getMethod())
      || isExcludePath(c.getUriInfo().getPath())) {
      return;
    }

    // need authorized
    String authorization = c.getHeaderString(JWT_HEADER_NAME);
    if (authorization == null || !authorization.startsWith(JWT_VALUE_PREFIX)) {
      abortRequest(c, Status.UNAUTHORIZED, "No valid jwt 'Authorization' header");
    } else {
      try {
        // verify and decode to a JWT instance
        authorization = authorization.substring(7); // "Bearer ".length() == 7
        logger.debug("jwt={}", authorization);
        JWT jwt = JWT.verify(authorization, secretKey);

        // set all payload data to context
        jwt.payload.getData().forEach(Context::set);
      } catch (DecodeException e) {
        if (logger.isDebugEnabled()) logger.debug(e.getMessage(), e);
        else logger.warn(e.getMessage());
        abortRequest(c, Status.UNAUTHORIZED, "Invalid JWT");
      }
    }
  }

  private boolean isExcludePath(String path) {
    if (isRootPath(path)) return true;
    else return hasExcludePaths && excludePaths.stream().anyMatch(path::startsWith);
  }

  private boolean isRootPath(String path) {
    return path.isEmpty() || path.equals("/") || path.equals("/index.html") || path.equals("/index.htm");
  }

  private void abortRequest(ContainerRequestContext c, Status status, String body) {
    c.abortWith(Response.status(status)
      .type(MediaType.TEXT_PLAIN)
      .entity(body)
      .build()
    );
  }
}