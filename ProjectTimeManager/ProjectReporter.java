package ProjectTimeManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProjectReporter {
  private final String _project_name;
  private final TimeLogManager _time_manager;

  private static final String _doc_head = "{\\rtf1\\ansi\\deff0";
  private static final String _doc_end = "}";
  private static final String _sep = System.lineSeparator();
  private static final String _path_config = ".config";
  private static final String _key_name = "name";
  private static final String _key_duration = "hours";
  public static final String[] config_keys = {_key_name, _key_duration};
  private final String _name;
  private final Duration _should_duration;

  private List<String> createGreeting() {
    List<String> content = new LinkedList<String>();
    final String formatted_date =
        String.format("\\qr \\sb300 {\\loch %s}", Interval.formatDate(LocalDateTime.now()));
    content.add(formatted_date);
    content.add(String.format("\\par \\pard \\sb300 \\plain {\\loch Dear %s,}", _name));
    return content;
  }

  private static String formatCell(final String content) {
    return "\\intbl " + content + " \\cell";
  }

  private List<String> createTable() {
    final Map<Long, ArrayList<Interval>> intervals_per_day = _time_manager.getGroupedIntervals();
    List<Long> sorted_days = new ArrayList<>(intervals_per_day.keySet());
    Collections.sort(sorted_days);

    assert !sorted_days.isEmpty() : "not interval found for the report";

    final String start_date_str =
        Interval.formatDate(intervals_per_day.get(sorted_days.get(0)).get(0).getStartTime());
    final String end_date_str =
        Interval.formatDate(
            intervals_per_day.get(sorted_days.get(sorted_days.size() - 1)).get(0).getStartTime());

    List<String> table = new LinkedList<String>();
    table.add(
        String.format(
            "\\par \\pard \\sb300 \\sa300 \\plain {\\loch your working time in %s from %s to %s is as follows:}",
            _project_name, start_date_str, end_date_str));
    table.add("\\par \\sb200 \\qc");
    final long should_millis = _should_duration.toMillis();
    long balance = 0l;
    long total_time = 0l;
    final int[] right_bounds = new int[] {700, 1600, 2500, 3400, 4400, 5400};
    final String cell_def =
        Arrays.stream(right_bounds)
            .mapToObj(i -> "\\cellx" + Integer.toString(i))
            .reduce("", (s, a) -> s + a);

    table.add("\\trowd \\trqc " + cell_def + _sep);
    table.add(formatCell("Date"));
    table.add(formatCell("Start"));
    table.add(formatCell("End"));
    table.add(formatCell("Elapsed"));
    table.add(formatCell("Sum"));
    table.add(formatCell("Change"));
    table.add("\\row \\pard" + _sep);
    for (final Long day : sorted_days) {
      final ArrayList<Interval> intervals_in_day = intervals_per_day.get(day);
      final long elapsed_millis =
          intervals_in_day.stream().map(Interval::getDurationMs).mapToLong(l -> l).sum();
      String text_day_sum = Interval.formatDuration(Duration.ofMillis(elapsed_millis));
      String text_date = intervals_in_day.get(0).formatDateInYear();
      String text_delta = Interval.formatDurationMillis(elapsed_millis - should_millis);
      balance += (elapsed_millis - should_millis);
      total_time += elapsed_millis;

      for (final Interval interval : intervals_in_day) {
        table.add("\\trowd \\trqc " + cell_def);
        table.add(formatCell(text_date));
        table.add(formatCell(Interval.formatClockTime(interval.getStartTime())));
        table.add(formatCell(Interval.formatClockTime(interval.getEndTime())));
        table.add(formatCell(interval.formatDuration()));
        table.add(formatCell(text_day_sum));
        table.add(formatCell(text_delta));
        table.add("\\row \\pard");

        text_date = "";
        text_day_sum = "";
        text_delta = "";
      }
    }
    final String summary =
        String.format(
            "The total working time for the %d days with time tracking is %s, the balance for this period is %s (with %s planned per day).",
            intervals_per_day.size(),
            Interval.removeSpaces(Interval.formatDuration(Duration.ofMillis(total_time))),
            Interval.formatDurationMillis(balance),
            Interval.formatDuration(_should_duration).replace(" ", ""));
    table.add("\\par \\pard \\sb300 \\plain {\\loch " + summary + "}" + _sep);
    return table;
  }

  private List<String> createGreetingAgain() {
    List<String> contents = new LinkedList<String>();
    contents.add("\\par \\pard \\sb300 \\plain {\\loch Sincerely yours}");
    contents.add("\\par \\pard \\sb300 \\plain {\\loch ProjectTimeManager from t-lou}");
    return contents;
  }

  public void output(final String filename) {
    List<String> contents = new LinkedList<String>();

    contents.add(_doc_head);
    contents.add("");
    contents.addAll(createGreeting());
    contents.add("");
    contents.addAll(createTable());
    contents.add("");
    contents.addAll(createGreetingAgain());
    contents.add("");
    contents.add(_doc_end);
    contents.add("");

    Utils.writeFile(filename, contents);
  }

  public static boolean isConfigReady() {
    if (!Files.exists(Paths.get(_path_config))) {
      return false;
    }
    final HashMap<String, String> config = loadConfigItems();
    return config != null && Arrays.stream(config_keys).allMatch(key -> config.containsKey(key));
  }

  public static void saveConfigItems(final HashMap<String, String> config) {
    List<String> contents = new LinkedList<String>();
    for (Map.Entry<String, String> entry : config.entrySet()) {
      contents.add(entry.getKey() + ": " + entry.getValue());
    }
    Utils.writeFile(_path_config, contents);
  }

  public static HashMap<String, String> loadConfigItems() {
    HashMap<String, String> config = null;

    if (!Files.exists(Paths.get(_path_config))) {
      return config;
    }

    try {
      for (String line : Utils.readFile(_path_config)) {
        assert (line.chars().map(ch -> (ch == ':' ? 1 : 0)).sum() == 1);
        final int sep = line.indexOf(':');
        final String key = line.substring(0, sep);
        String value = line.substring(sep + 1);
        while (!value.isEmpty() && value.charAt(0) == ' ') {
          value = value.substring(1);
        }
        if (Arrays.asList(config_keys).contains(key)) {
          if (config == null) {
            config = new HashMap<String, String>();
          }
          config.put(key, value);
        }
      }
    } catch (Exception ex) {
      assert 1 == 2 : "error loading the config for reporter";
      return null;
    }

    return config;
  }

  public ProjectReporter(final String project_name) {
    _project_name = project_name;
    _time_manager = new ProjectManager(project_name).getLogManager();

    final HashMap<String, String> config = loadConfigItems();
    _name = config.get(_key_name);
    String text_duration = config.get(_key_duration).replace(",", ".");
    _should_duration = Duration.ofMinutes((long) (Float.parseFloat(text_duration) * 60.0));
  }
}
