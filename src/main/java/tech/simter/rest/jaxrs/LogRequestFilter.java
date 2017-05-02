package tech.simter.rest.jaxrs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Log out the request info. Include method, path and accept header
 *
 * @author RJ 2017-04-29
 */
@Named
@Singleton
@Provider
public class LogRequestFilter implements ContainerRequestFilter {
  private static Logger logger = LoggerFactory.getLogger(LogRequestFilter.class);

  @Override
  public void filter(ContainerRequestContext c) throws IOException {
    if (logger.isDebugEnabled())
      logger.debug("{} {} Accept={}", c.getMethod(), c.getUriInfo().getAbsolutePath(),
        c.getHeaderString("Accept"));
  }
}