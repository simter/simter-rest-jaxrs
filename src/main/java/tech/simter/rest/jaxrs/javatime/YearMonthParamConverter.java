package tech.simter.rest.jaxrs.javatime;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.ext.ParamConverter;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Named
@Singleton
public class YearMonthParamConverter implements ParamConverter<YearMonth> {
  private final static DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyyMM");

  @Override
  public YearMonth fromString(String yearMonth) {
    if (null == yearMonth) return null;

    if (yearMonth.length() != 6)
      throw new IllegalStateException("Could not convert '" + yearMonth + "' to java.time.YearMonth.");
    return YearMonth.of(Integer.parseInt(yearMonth.substring(0, 4)), Integer.parseInt(yearMonth.substring(4)));
  }

  @Override
  public String toString(YearMonth yearMonth) {
    return yearMonth == null ? null : yearMonth.format(f);
  }
}