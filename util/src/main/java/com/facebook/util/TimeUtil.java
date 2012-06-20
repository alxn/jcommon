package com.facebook.util;

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.chrono.ISOChronology;

import java.util.Map;

public class TimeUtil {
  private static final Logger LOG = Logger.getLogger(TimeUtil.class);

  // DateTimeZone.forID() and ISOChronology.getInstance() are very expensive,
  // so we statically pre-compute a fast lookup table.
  private static final Map<String, DateTimeZone> TIME_ZONE_MAP;
  private static final Map<String, ISOChronology> CHRONOLOGY_MAP;

  static {
    ImmutableMap.Builder<String, DateTimeZone> timeZoneBuilder =
      new ImmutableMap.Builder<String, DateTimeZone>();
    ImmutableMap.Builder<String, ISOChronology> chronologyBuilder =
      new ImmutableMap.Builder<String, ISOChronology>();

    for (Object id : DateTimeZone.getAvailableIDs()) {
      String tz = (String) id;
      DateTimeZone timeZone = DateTimeZone.forID(tz);
      timeZoneBuilder.put(tz, timeZone);
      chronologyBuilder.put(tz, ISOChronology.getInstance(timeZone));
    }
    TIME_ZONE_MAP = timeZoneBuilder.build();
    CHRONOLOGY_MAP = chronologyBuilder.build();
  }

  // utility method to log how long a chunk of code takes to run
  public static <E extends Throwable> void logElapsedTime(
    String tag, ExtRunnable<E> task
  ) throws E {
    long start = DateTimeUtils.currentTimeMillis();
    boolean success = false;

    try {
      task.run();
      success = true;
    } finally {
      LOG.info(String.format(
        "%s (%b) elapsed time(ms): %d",
        tag,
        success,
        DateTimeUtils.currentTimeMillis() - start
      ));
    }
  }

  // utility method to log how long a chunk of code takes to run
  public static <V, E extends Throwable> V logElapsedTime(
    String tag, ExtCallable<V, E> task
  ) throws E {
    long start = DateTimeUtils.currentTimeMillis();
    boolean success = false;

    try {
      V value = task.call();
      success = true;

      return value;
    } finally {
      LOG.info(String.format(
        "%s (%b) elapsed time(ms): %d",
        tag,
        success,
        DateTimeUtils.currentTimeMillis() - start
      ));
    }
  }

  public static DateTimeZone getDateTimeZone(String dateTimeZoneStr) {
      if ((dateTimeZoneStr == null) || dateTimeZoneStr.isEmpty()) {
        return DateTimeZone.UTC;
      }
      return TIME_ZONE_MAP.get(dateTimeZoneStr);
    }

  public static ISOChronology getChronology(String dateTimeZoneStr) {
    if ((dateTimeZoneStr == null) || dateTimeZoneStr.isEmpty()) {
      dateTimeZoneStr = DateTimeZone.UTC.getID();
    }
    return CHRONOLOGY_MAP.get(dateTimeZoneStr);
  }

  /**
   * these methods affect only code that relies on DateTimeUtils.currentTimeMillis()
   *
   * NOTE: manipulation of {@link DateTimeUtils.currentTimeMillis()} is not thread safe
   * to begin with, so neither is this
   */
  public static void setNow(DateTime now) {
    DateTimeUtils.setCurrentMillisFixed(now.getMillis());
  }

  public static void advanceNow(Duration duration) {
    long now = DateTimeUtils.currentTimeMillis();

    DateTimeUtils.setCurrentMillisFixed(now + duration.getMillis());
  }
}
