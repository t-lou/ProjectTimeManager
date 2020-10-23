package ProjectTimeManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Interval {
  /** Starting time. */
  private final LocalDateTime _time_start;

  /** End time. */
  private final LocalDateTime _time_end;

  /** The formatter between timestamp and text. */
  private static DateTimeFormatter _formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /** Time format for yyyy-MM-dd. */
  private static DateTimeFormatter _formatter_date = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /** Time format for MM-dd. */
  private static DateTimeFormatter _formatter_date_in_year = DateTimeFormatter.ofPattern("MM-dd");

  /** The format for getting month id. */
  private static DateTimeFormatter _formatter_month = DateTimeFormatter.ofPattern("yyyy-MM");

  /** The format for getting clock time. */
  private static DateTimeFormatter _formatter_clock = DateTimeFormatter.ofPattern("HH:mm:ss");

  /**
   * Read and parse the timestamp from the given text.
   *
   * @param text The text to read.
   * @return The timestamp represented in the given text.
   */
  public static LocalDateTime parseDateTime(String text) {
    return LocalDateTime.parse(text, _formatter);
  }

  /**
   * Get the hr:min:sec part of the formatted text.
   *
   * @param text The formatted text for one time.
   * @return The text representing the time in one day.
   */
  public static String trimTimeInDay(final String formatted_data) {
    return formatted_data.substring(formatted_data.indexOf(' ') + 1);
  }

  /** Get the formatted time to present the start time. */
  public String formatStartTime() {
    return getStartTime().format(_formatter);
  }

  /** Get the formatted time to present the end time. */
  public String formatEndTime() {
    return getEndTime().format(_formatter);
  }

  /**
   * Return the text which represents the given timestamp.
   *
   * @return The text for this timestamp.
   */
  public String formatInterval() {
    return formatStartTime() + " - " + formatEndTime();
  }

  /**
   * Get the duration of this interval in millisecond.
   *
   * @return The duration of this interval in millisecond.
   */
  public long getDurationMs() {
    return Duration.between(_time_start, _time_end).toMillis();
  }

  /**
   * Get a text which represents the duration in hour, minute and second.
   *
   * @param duration Duration to show.
   * @return Text which shows the duration.
   */
  public static String formatDuration(final Duration duration) {
    return LocalDateTime.of(1992, 01, 15, 00, 00).plus(duration).format(_formatter_clock);
  }

  public static String formatDurationMillis(final long millis) {
    return (millis >= 0l ? "+" : "-")
        + removeSpaces(formatDuration(Duration.ofMillis(Math.abs(millis))));
  }

  /** Get the formatted text to present this duration. */
  public String formatDuration() {
    return formatDuration(Duration.between(_time_start, _time_end));
  }

  /**
   * Remove empty spaces in the string.
   *
   * @param content Original text.
   * @return Text without empty spaces.
   */
  public static String removeSpaces(final String content) {
    return content.replace(" ", "");
    // return content
    //     .chars()
    //     .filter(ch -> ch != ' ')
    //     .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
    //     .toString();
  }

  /**
   * Get the starting time.
   *
   * @return The starting time.
   */
  public LocalDateTime getStartTime() {
    return _time_start;
  }

  /**
   * Get the starting time.
   *
   * @return The starting time.
   */
  public LocalDateTime getEndTime() {
    return _time_end;
  }

  /** Get one id to represent the month of input time. */
  public static String formatMonth(final LocalDateTime time) {
    return time.format(_formatter_month);
  }

  /** Get the formatted text to present the date for the input time (MM-DD). */
  public static String formatDateInYear(final LocalDateTime time) {
    return time.format(_formatter_date_in_year);
  }

  /** Get the formatted text to present the date for the input time (yyyy-MM-DD). */
  public static String formatDate(final LocalDateTime time) {
    return time.format(_formatter_date);
  }

  /** Get the formatted text to present the date for the input time (HH:mm:ss). */
  public static String formatClockTime(final LocalDateTime time) {
    return time.format(_formatter_clock);
  }

  /** Get formatted string to represent the start date. */
  public String formatDateInYear() {
    return formatDateInYear(_time_start);
  }

  public Interval(String text) {
    final String[] slices = text.split(" - ");

    assert slices.length == 2 : "Line in log cannot be parsed.";

    _time_start = parseDateTime(slices[0]);
    _time_end = parseDateTime(slices[1]);
  }

  public Interval(LocalDateTime time_start, LocalDateTime time_end) {
    _time_start = time_start;
    _time_end = time_end;
  }
}
