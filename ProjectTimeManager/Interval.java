package ProjectTimeManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Interval {
  /** Starting time. */
  private LocalDateTime _time_start;

  /** End time. */
  private LocalDateTime _time_end;

  /** The pattern for saving the timestamp in text. */
  private static String _pattern = "dd/MM/yyyy HH:mm:ss";

  /** The formatter between timestamp and text. */
  private static DateTimeFormatter _formatter = DateTimeFormatter.ofPattern(_pattern);

  /**
   * Read and parse the timestamp from the given text.
   *
   * @param text The text to read.
   * @return The timestamp represented in the given text.
   */
  private static LocalDateTime parseDateTime(String text) {
    return LocalDateTime.parse(text, _formatter);
  }

  /**
   * Get the hr:min:sec part of the formatted text.
   *
   * @param text The formatted text for one time.
   * @return The text representing the time in one day.
   */
  public static String trimTimeInDay(final String formatted_data) {
    return formatted_data.substring(11);
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
  public String formatDateTime() {
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
  public static String getTextForDuration(final Duration duration) {
    final String text =
        String.format(
            "%3d:%02d:%02d",
            duration.toHours(),
            duration.toMinutes() - duration.toHours() * 60L,
            duration.toMillis() / 1000L - duration.toMinutes() * 60L);

    return text;
  }

  public static String formatDurationMillis(final long millis) {
    return (millis >= 0l ? "+" : "-")
        + removeSpaces(getTextForDuration(Duration.ofMillis(Math.abs(millis))));
  }

  /** Get the formatted text to present this duration. */
  public String getTextForDuration() {
    return getTextForDuration(Duration.between(_time_start, _time_end));
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

  /** Get the string to represent day, month, year and HH:MM:SS time. */
  private static String[] getInfoInStrings(final LocalDateTime time) {
    final String[] parts = time.format(_formatter).split("[/ ]");
    assert (parts.length == 4);
    return parts;
  }

  /** Get one id to represent the month of input time. */
  public static String getMonthId(final LocalDateTime time) {
    final String[] parts = getInfoInStrings(time);
    return parts[1] + "_" + parts[2];
  }

  /** Get the formatted text to present the month for the input time (MM/YYYY). */
  public static String getMonthAndYear(final LocalDateTime time) {
    final String[] parts = getInfoInStrings(time);
    return parts[1] + "/" + parts[2];
  }

  /** Get the formatted text to present the date for the input time (DD/MM). */
  public static String getDateInYear(final LocalDateTime time) {
    final String[] parts = getInfoInStrings(time);
    return parts[0] + "/" + parts[1];
  }

  /** Get the formatted text to present the date for the input time (DD/MM/YYYY). */
  public static String getDate(final LocalDateTime time) {
    final String[] parts = getInfoInStrings(time);
    return parts[0] + "/" + parts[1] + "/" + parts[2];
  }

  /** Get formatted string to represent the start date. */
  public String getDateInYear() {
    return getDateInYear(_time_start);
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
