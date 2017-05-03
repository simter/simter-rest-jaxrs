package tech.simter.rest.jaxrs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Log out the response info. Include status, headers and body
 *
 * @author RJ 2017-04-29
 */
@Named
@Singleton
@Provider
@Priority(Priorities.AUTHENTICATION - 100)
public class LogResponseFilter implements ContainerResponseFilter {
  private static Logger logger = LoggerFactory.getLogger(LogResponseFilter.class);

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext c) throws IOException {
    if (logger.isDebugEnabled()) { // log out status, headers and body
      final StringBuilder s = new StringBuilder(c.getStatus() + " " + c.getStatusInfo().getReasonPhrase());
      // headers
      s.append("\r\nResponse Headers:");
      String collect = c.getHeaders().entrySet().stream()
        .map(e -> e.getKey() + ": " +
          (e.getValue() != null && e.getValue().size() == 1 ? e.getValue().get(0) : e.getValue().toString()))
        .collect(Collectors.joining("\r\n  "));
      if (!collect.isEmpty()) s.append("\r\n  ").append(collect);

      // body
      if (c.hasEntity()) {
        s.append("\r\nResponse Body:\r\n  ");
        s.append(c.getEntity().toString());
      }

      logger.debug(s.toString());
    } else if (logger.isInfoEnabled()) {  // just log out status
      logger.info("{} {}", c.getStatus(), c.getStatusInfo().getReasonPhrase());
    }
  }
}