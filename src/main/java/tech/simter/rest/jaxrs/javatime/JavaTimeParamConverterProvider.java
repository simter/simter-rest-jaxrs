package tech.simter.rest.jaxrs.javatime;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.YearMonth;

@Named
@Singleton
@Provider
public class JavaTimeParamConverterProvider implements ParamConverterProvider {
  @Inject
  private YearMonthParamConverter yearMonthParamConverter;

  @Override
  @SuppressWarnings("unchecked")
  public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
    if (rawType.equals(YearMonth.class)) return (ParamConverter<T>) yearMonthParamConverter;
    else return null;
  }
}