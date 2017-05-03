package tech.simter.rest.jaxrs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

/**
 * Log out the request info. Include method, headers and body
 *
 * @author RJ 2017-04-29
 */
@Named
@Singleton
@Provider
@Priority(Priorities.AUTHENTICATION - 100)
public class LogRequestFilter implements ContainerRequestFilter {
  private static Logger logger = LoggerFactory.getLogger(LogRequestFilter.class);

  @Override
  public void filter(ContainerRequestContext c) throws IOException {
    if (logger.isDebugEnabled()) { // log out method, path, headers and body
      final StringBuilder s = new StringBuilder(c.getMethod() + " " + c.getUriInfo().getAbsolutePath());

      // headers
      s.append("\r\nRequest Headers:");
      String collect = c.getHeaders().entrySet().stream()
        .map(e -> e.getKey() + ": " +
          (e.getValue() != null && e.getValue().size() == 1 ? e.getValue().get(0) : e.getValue().toString()))
        .collect(Collectors.joining("\r\n  "));
      if (!collect.isEmpty()) s.append("\r\n  ").append(collect);

      // body
      if (c.hasEntity()) {
        s.append("\r\nRequest Body:\r\n  ");

        //org.springframework.util.StreamUtils.copyToString(c.getEntityStream(), Charset.forName("UTF-8"));
        StringBuilder out = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(c.getEntityStream(), Charset.forName("UTF-8"));
        char[] buffer = new char[4096];
        int bytesRead;
        while ((bytesRead = reader.read(buffer)) != -1) {
          out.append(buffer, 0, bytesRead);
        }
        s.append(out);

        logger.debug(s.toString());

        // because only can read the request stream just once, so need to reset it again.
        c.setEntityStream(new ByteArrayInputStream(out.toString().getBytes(Charset.forName("UTF-8"))));
      } else if (logger.isInfoEnabled()) {  // just log out method and path
        logger.info("{} {}", c.getMethod(), c.getUriInfo().getAbsolutePath());
      }
    }
  }
}