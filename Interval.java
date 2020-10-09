import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

  public String formatStartTime() {
    return getStartTime().format(_formatter);
  }

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

  public String getTextForDuration() {
    return getTextForDuration(Duration.between(_time_start, _time_end));
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

  private static String[] getInfoInStrings(final LocalDateTime time) {
    final String[] parts = time.format(_formatter).split("[/ ]");
    assert (parts.length == 4);
    return parts;
  }

  public static String getMonthId(final LocalDateTime time) {
    final String[] parts = getInfoInStrings(time);
    return parts[1] + "_" + parts[2];
  }

  public static String getMonthAndYear(final LocalDateTime time) {
    final String[] parts = getInfoInStrings(time);
    return parts[1] + "/" + parts[2];
  }

  public static String getDateInYear(final LocalDateTime time) {
    final String[] parts = getInfoInStrings(time);
    return parts[0] + "/" + parts[1];
  }

  public static String getDate(final LocalDateTime time) {
    final String[] parts = getInfoInStrings(time);
    return parts[0] + "/" + parts[1] + "/" + parts[2];
  }

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
