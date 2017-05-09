package tech.simter.rest.jaxrs;

import javax.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Set the response status to {@link javax.ws.rs.core.Response.Status#CREATED} by annotation.
 *
 * @author RJ
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface CreatedStatus {
}