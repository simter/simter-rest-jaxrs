package tech.simter.rest.jaxrs;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * The filter for set response default charset.
 * Context initiator can use the constructor to control the the default charset. Default to utf-8 if no setting.
 *
 * @author RJ 2017-04-29
 */
@Named
@Singleton
@Provider
public class CharsetResponseFilter implements ContainerResponseFilter {
  /**
   * The Default charset
   */
  public final String DEFAULT_CHARSET = "utf-8";
  private String charset;

  public CharsetResponseFilter() {
    charset = DEFAULT_CHARSET;
  }

  public CharsetResponseFilter(String charset) {
    if (charset != null) this.charset = charset;
    else this.charset = DEFAULT_CHARSET;
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
    throws IOException {
    MediaType type = responseContext.getMediaType();
    if (type != null) {
      if (!type.getParameters().containsKey(MediaType.CHARSET_PARAMETER)) {
        MediaType typeWithCharset = type.withCharset(charset);
        responseContext.getHeaders().putSingle("Content-Type", typeWithCharset);
      }
    }
  }
}