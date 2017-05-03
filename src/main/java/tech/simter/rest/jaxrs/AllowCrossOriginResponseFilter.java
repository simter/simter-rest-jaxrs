package tech.simter.rest.jaxrs;

import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Priority;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static tech.simter.rest.jaxrs.JwtAuthRequestFilter.JWT_HEADER_NAME;

/**
 * The filter for allow cross origin request
 *
 * @author RJ
 */
@Named
@Singleton
@Provider
@Priority(Priorities.USER - 1)
public class AllowCrossOriginResponseFilter implements ContainerResponseFilter {
  private List<String> allowOrigins;
  private List<String> allowMethods;
  private int maxAge; // seconds

  /**
   * See also http://www.ruanyifeng.com/blog/2016/04/cors.html.
   *
   * @param allowOrigins the Access-Control-Allow-Origin
   * @param allowMethods the Access-Control-Allow-Methods
   * @param maxAge       the Access-Control-Max-Age, zero to ignore, default to 10 seconds
   */
  public AllowCrossOriginResponseFilter(@Value("${simter.cors.allow-origins:#{null}}") List<String> allowOrigins,
                                        @Value("${simter.cors.allow-methods:#{null}}") List<String> allowMethods,
                                        @Value("${simter.cors.max-age:#{10}}") int maxAge) {
    this.allowOrigins = allowOrigins == null ? Collections.emptyList() : allowOrigins;
    this.allowMethods = allowMethods == null ? Collections.emptyList() : allowMethods;
    this.maxAge = maxAge;
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
    throws IOException {
    MultivaluedMap<String, Object> responseHeaders = responseContext.getHeaders();

    if (HttpMethod.OPTIONS.equalsIgnoreCase(requestContext.getMethod())) {  // pre-flight request
      String requestOrigin = requestContext.getHeaderString("Origin");
      String requestHeaders = requestContext.getHeaderString("Access-Control-Request-Headers");
      String requestMethod = requestContext.getHeaderString("Access-Control-Request-Method");

      // only allow JWT_HEADER_NAME
      if (!JWT_HEADER_NAME.equalsIgnoreCase(requestHeaders) || requestOrigin == null || requestMethod == null) {
        requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        return;
      }

      if ((allowOrigins.isEmpty() || "*".equals(allowOrigins.get(0)))       // allow all origins
        || allowOrigins.contains(requestOrigin)) {
        responseHeaders.putSingle("Access-Control-Allow-Origin", requestOrigin);
        if (allowMethods.isEmpty() || allowMethods.contains(requestMethod))
          responseHeaders.putSingle("Access-Control-Allow-Methods", requestMethod);
        responseHeaders.putSingle("Access-Control-Allow-Headers", requestHeaders);
        if (maxAge > 0) responseHeaders.putSingle("Access-Control-Max-Age", maxAge);
        responseContext.setStatusInfo(Response.Status.NO_CONTENT);
        //responseContext.setEntity(null, null, MediaType.TEXT_PLAIN_TYPE);
      } else {                                                              // not allow cors
        requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
      }
    } else {                                                                // real request
      String requestOrigin = requestContext.getHeaderString("Origin");
      if (requestOrigin != null && requestContext.getHeaderString(JWT_HEADER_NAME) != null)
        responseHeaders.putSingle("Access-Control-Allow-Origin", requestOrigin);
    }
  }
}