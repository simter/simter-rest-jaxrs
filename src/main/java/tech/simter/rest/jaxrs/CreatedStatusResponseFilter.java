package tech.simter.rest.jaxrs;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Set the response status by {@link CreatedStatus} annotation.
 *
 * @author RJ
 */
@Named
@Singleton
@Provider
@CreatedStatus
public class CreatedStatusResponseFilter implements ContainerResponseFilter {
  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
    throws IOException {
    responseContext.setStatusInfo(Response.Status.CREATED);
  }
}