package tech.simter.rest.jaxrs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Map;

/**
 * The deepest base exception mapper
 *
 * @author RJ 2017-04-29
 */
@Named
@Singleton
@Provider
public class ThrowableExceptionMapper implements ExceptionMapper<Throwable> {
  private final static Logger logger = LoggerFactory.getLogger(ThrowableExceptionMapper.class);
  private final Map<Class<?>, Object> mapper;

  public ThrowableExceptionMapper() {
    mapper = null;
  }

  public ThrowableExceptionMapper(Map<Class<?>, Object> mapper) {
    this.mapper = mapper;
  }

  @Override
  public Response toResponse(Throwable e) {
    logger.warn(e.getMessage(), e);

    // ignore jav-rs standard exception
    if (e instanceof WebApplicationException) return ((WebApplicationException) e).getResponse();
    if (e instanceof ProcessingException) throw (ProcessingException) e;

    Response.ResponseBuilder b = getErrorResponseBuilder().type(MediaType.TEXT_PLAIN);
    if (mapper != null && mapper.containsKey(e.getClass())) {   // use mapper
      Object errorMapper = mapper.get(e.getClass());
      if (errorMapper instanceof ErrorMapper) {
        b.status(((ErrorMapper) errorMapper).status);
        b.entity(((ErrorMapper) errorMapper).entity);
      } else {
        b.entity(errorMapper);                             // use exception message as body
      }
    } else {
      String msg = getDeepMessage(e);
      if (msg == null || msg.isEmpty()) b.entity(e.toString()); // use class name as body
      else b.entity(msg);                                       // use custom message as body
    }
    return b.build();
  }

  public Response.ResponseBuilder getErrorResponseBuilder() {
    return Response.serverError();
  }

  /**
   * Recursive to get the exception message.
   *
   * @param e the exception
   * @return the exception message
   */
  private static String getDeepMessage(Throwable e) {
    if (e == null) return null;
    if (e.getCause() == null) return e.getMessage();

    return getDeepMessage(e.getCause());
  }

  /**
   * Define the error mapper.
   */
  public static class ErrorMapper {
    private Response.Status status;
    private Object entity;

    public ErrorMapper(Object entity) {
      this.status = Response.Status.INTERNAL_SERVER_ERROR;
      this.entity = entity;
    }

    public ErrorMapper(Response.Status status, Object entity) {
      this.status = status;
      this.entity = entity;
    }
  }
}