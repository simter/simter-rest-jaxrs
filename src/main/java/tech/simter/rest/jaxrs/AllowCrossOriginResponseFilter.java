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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static tech.simter.rest.jaxrs.JwtAuthRequestFilter.JWT_HEADER_NAME;

/**
 * The filter for allow cross origin request
 *
 * @author RJ
 */
@Named
@Singleton
@Provider
@Priority(Priorities.USER + 100)
public class AllowCrossOriginResponseFilter implements ContainerResponseFilter {
  private List<String> allowOrigins;
  private List<String> allowMethods;
  private List<String> allowHeaders;
  private int maxAge; // seconds

  /**
   * See also http://www.ruanyifeng.com/blog/2016/04/cors.html.
   *
   * @param allowOrigins the Access-Control-Allow-Origin
   * @param allowMethods the Access-Control-Allow-Methods
   * @param allowHeaders the Access-Control-Allow-Headers
   * @param maxAge       the Access-Control-Max-Age, zero to ignore, default to 10 seconds
   */
  public AllowCrossOriginResponseFilter(@Value("${simter.cors.allow-origins:#{null}}") List<String> allowOrigins,
                                        @Value("${simter.cors.allow-methods:#{null}}") List<String> allowMethods,
                                        @Value("${simter.cors.allow-headers:#{null}}") List<String> allowHeaders,
                                        @Value("${simter.cors.max-age:#{10}}") int maxAge) {
    this.allowOrigins = allowOrigins == null ? Collections.emptyList() : lowerCase(allowOrigins);
    this.allowMethods = allowMethods == null ? Collections.emptyList() : lowerCase(allowMethods);
    this.allowHeaders = allowHeaders == null ? Collections.emptyList() : lowerCase(allowHeaders);
    this.maxAge = maxAge;
  }

  // do lowerCase
  private List<String> lowerCase(List<String> sources) {
    return sources.stream().map(String::toLowerCase).collect(Collectors.toList());
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
    throws IOException {
    MultivaluedMap<String, Object> responseHeaders = responseContext.getHeaders();

    if (HttpMethod.OPTIONS.equalsIgnoreCase(requestContext.getMethod())) {  // pre-flight request
      responseContext.setEntity(null);
      String requestOrigin = requestContext.getHeaderString("Origin");
      String requestHeaders = requestContext.getHeaderString("Access-Control-Request-Headers");
      String requestMethod = requestContext.getHeaderString("Access-Control-Request-Method");

      if (requestOrigin == null || requestMethod == null ||
        // confirm allow origins
        !(allowOrigins.isEmpty() || "*".equals(allowOrigins.get(0))      // allow all origins
          || allowOrigins.contains(requestOrigin)) ||
        // confirm allow headers
        !(allowHeaders.isEmpty() || "*".equals(allowHeaders.get(0))     // allow all headers
          || allowHeaders.containsAll(Arrays.asList(requestHeaders.toLowerCase().split(","))))
        ) {
        responseContext.setStatusInfo(Response.Status.FORBIDDEN);
        return;
      }

      // allow origin
      responseHeaders.putSingle("Access-Control-Allow-Origin", requestOrigin);

      // allow methods
      if (allowMethods.isEmpty() || allowMethods.contains(requestMethod))
        responseHeaders.putSingle("Access-Control-Allow-Methods", requestMethod);

      // allow headers
      // -- auto expose 'Content-Disposition' header for 'application/octet-stream' download
      if (requestHeaders.contains("content-disposition")) {
        responseHeaders.putSingle("Access-Control-Expose-Headers", "content-disposition");
      }
      responseHeaders.putSingle("Access-Control-Allow-Headers", requestHeaders);

      // maxAge
      if (maxAge > 0) responseHeaders.putSingle("Access-Control-Max-Age", maxAge);

      responseContext.setStatusInfo(Response.Status.NO_CONTENT);
    } else {                                                                // real request
      String requestOrigin = requestContext.getHeaderString("Origin");
      if (requestOrigin != null && requestContext.getHeaderString(JWT_HEADER_NAME) != null)
        responseHeaders.putSingle("Access-Control-Allow-Origin", requestOrigin);
    }
  }
}