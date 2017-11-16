package kurtome.dote.server.controllers.podcast

import java.time.chrono.IsoChronology
import java.time.format._
import java.time.temporal.ChronoField._
import java.time.temporal.{TemporalAccessor, TemporalField}

import collection.JavaConverters._
import scala.collection.mutable

object RichRfc1123DateTimeParser {

  // custom map for days of week
  private val dayOfWeek: java.util.Map[java.lang.Long, java.lang.String] = mutable
    .Map[java.lang.Long, java.lang.String](
      java.lang.Long.valueOf(1L) -> "Mon",
      java.lang.Long.valueOf(2L) -> "Tue",
      java.lang.Long.valueOf(3L) -> "Wed",
      java.lang.Long.valueOf(4L) -> "Thu",
      java.lang.Long.valueOf(5L) -> "Fri",
      java.lang.Long.valueOf(6L) -> "Sat",
      java.lang.Long.valueOf(7L) -> "Sun"
    )
    .asJava
  // custom map for months
  private val monthOfYear: java.util.Map[java.lang.Long, String] = Map[java.lang.Long, String](
    java.lang.Long.valueOf(1L) -> "Jan",
    java.lang.Long.valueOf(2L) -> "Feb",
    java.lang.Long.valueOf(3L) -> "Mar",
    java.lang.Long.valueOf(4L) -> "Apr",
    java.lang.Long.valueOf(5L) -> "May",
    java.lang.Long.valueOf(6L) -> "Jun",
    java.lang.Long.valueOf(7L) -> "Jul",
    java.lang.Long.valueOf(8L) -> "Aug",
    java.lang.Long.valueOf(9L) -> "Sep",
    java.lang.Long.valueOf(10L) -> "Oct",
    java.lang.Long.valueOf(11L) -> "Nov",
    java.lang.Long.valueOf(12L) -> "Dec"
  ).asJava

  // create with same format as RFC_1123_DATE_TIME
  private val formatter: DateTimeFormatter = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .parseLenient()
    .optionalStart()
    .appendText(DAY_OF_WEEK.asInstanceOf[TemporalField], dayOfWeek)
    .appendLiteral(", ")
    .optionalEnd()
    .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
    .appendLiteral(' ')
    .appendText(MONTH_OF_YEAR, monthOfYear)
    .appendLiteral(' ')
    .appendValue(YEAR, 4) // 2 digit year not handled
    .appendLiteral(' ')
    .appendValue(HOUR_OF_DAY, 2)
    .appendLiteral(':')
    .appendValue(MINUTE_OF_HOUR, 2)
    .optionalStart()
    .appendLiteral(':')
    .appendValue(SECOND_OF_MINUTE, 2)
    .optionalEnd()
    .appendLiteral(' ')
    // difference from RFC_1123_DATE_TIME: optional offset OR zone ID
    .optionalStart()
    .appendZoneText(TextStyle.SHORT)
    .optionalEnd()
    .optionalStart()
    .appendOffset("+HHMM", "GMT")
    // use the same resolver style and chronology
    .toFormatter()
    .withResolverStyle(ResolverStyle.SMART)
    .withChronology(IsoChronology.INSTANCE);

  def parse(s: String): TemporalAccessor = {
    formatter.parse(s)
  }
}
