package ProjectTimeManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TimeLogManager {
  /** The list of cached timestamps. */
  private ArrayList<Interval> _time_entries = new ArrayList<>();

  /** Starting time of the latest interval (on-going interval if this project is opened). */
  private LocalDateTime _time_start;

  /** The number of seconds in one day. */
  public static final long SECONDS_PER_DAY = 24L * 3600L;

  /**
   * Get the starting second of the date time.
   *
   * @param time Time.
   * @return The starting second of the day.
   */
  public static long getSecondStartOfDay(final LocalDateTime time) {
    return time.toLocalDate().toEpochDay() * SECONDS_PER_DAY;
  }

  /**
   * Write the recorded time entries to file with given filename.
   *
   * @param filename The given filename.
   */
  public void updateLog(String filename) {
    List<String> contents = new LinkedList<String>();

    for (Interval interval : _time_entries) {
      contents.add(interval.formatInterval());
    }

    Utils.writeFile(filename, contents);
  }

  public void closeNow() {
    _time_entries.add(new Interval(_time_start, LocalDateTime.now()));
    assert areLogsOrdered() : "intervals are not in order";
  }

  /**
   * Checks whether the time logs are ordered, incl. whehther all intervals have later end time and
   * whether the starting time of one interval is later than the ending time of the earlier ones.
   *
   * @return
   */
  private boolean areLogsOrdered() {
    if (_time_entries.isEmpty()) {
      return true;
    } else {
      ArrayList<Long> all_time_ms = new ArrayList<>();
      for (final Interval interval : _time_entries) {
        all_time_ms.add(interval.getStartTime().toEpochSecond(ZoneOffset.UTC));
        all_time_ms.add(interval.getEndTime().toEpochSecond(ZoneOffset.UTC));
      }

      return IntStream.range(1, all_time_ms.size())
          .allMatch(i -> all_time_ms.get(i) > all_time_ms.get(i - 1));
    }
  }

  /**
   * Read the time entries which are stored in the file with given filename.
   *
   * @param filename The filename of the logging file.
   * @return Whether reading the time log is successful, it is successful only if the file exists
   *     and contains even number of entries.
   */
  public boolean readLog(final String filename) {
    _time_entries.clear();
    return addLog(filename);
  }

  public boolean addLog(final String filename) {
    final List<String> contents = Utils.readFile(filename);
    if (contents == null || contents.isEmpty()) {
      return false;
    }
    for (final String line : contents) {
      _time_entries.add(new Interval(line));
    }
    assert areLogsOrdered() : "logs are not aligned in file " + filename;
    return true;
  }

  /** Add current timestamp to the cache list. */
  public void addNow() {
    _time_start = LocalDateTime.now();
    // record this timestamp
    System.out.println("Added now as starting time " + _time_start.toString());
  }

  /**
   * Check whether interval is on one of the given dates. If dates is not given, then return false;
   *
   * @param interval The interval to check.
   * @param dates The list of dates.
   * @return Whether the dates are given and the interval is on one of the dates.
   */
  public static boolean isIntervalOnDates(Interval interval, ArrayList<Instant> dates) {
    return dates != null
        && !dates.isEmpty()
        && (dates.stream()
            .map(Instant::getEpochSecond)
            .collect(Collectors.toList())
            .contains(getSecondStartOfDay(interval.getStartTime())));
  }

  /**
   * Get the total elapsed time of all logped intervals in millisecond.
   *
   * @return The total elapsed time of all logped intervals in millisecond.
   */
  public long getTotalTimeMs(ArrayList<Instant> dates) {
    if (dates == null || dates.isEmpty()) {
      return _time_entries.stream().map(Interval::getDurationMs).mapToLong(l -> l).sum();
    } else {
      return _time_entries.stream()
          .filter(interval -> isIntervalOnDates(interval, dates))
          .map(Interval::getDurationMs)
          .mapToLong(l -> l)
          .sum();
    }
  }

  /**
   * Group the logged intervals with days from Posix time and return it as HashMap.
   *
   * @return HashMap with date from Posix epoch sa key and logged intervals as values.
   */
  public HashMap<Long, ArrayList<Interval>> getGroupedIntervals() {
    HashMap<Long, ArrayList<Interval>> map = new HashMap<>();

    for (Interval interval : _time_entries) {
      final Long day = interval.getStartTime().toLocalDate().toEpochDay();
      if (!map.containsKey(day)) {
        map.put(day, new ArrayList<>());
      }
      map.get(day).add(interval);
    }

    return map;
  }

  /** Get the intervals in this project. */
  public ArrayList<Interval> getIntervals() {
    return _time_entries;
  }

  /** Get the start time of this project. */
  public LocalDateTime getStartTime() {
    return _time_start;
  }

  /** Get one id name for current month for check in. */
  public static String getCurrnetMonthId() {
    return Interval.formatMonth(LocalDateTime.now());
  }

  public static void updateThisSession(final String filename, final String text) {
    List<String> contents = new LinkedList<String>();
    contents.add(text);
    Utils.writeFile(filename, contents);
  }

  public void updateThisSession(final String filename) {
    updateThisSession(filename, new Interval(_time_start, LocalDateTime.now()).formatInterval());
  }

  public TimeLogManager() {}
}
