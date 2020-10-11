package ProjectTimeManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProjectReporter {

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

  private String createGreeting() {
    final String start_date_str =
        Interval.getMonthAndYear(_time_manager.getIntervals().get(0).getStartTime());
    final String formatted_date =
        String.format("\\qr \\sb300 {\\loch %s}", Interval.getDate(LocalDateTime.now()));
    String content = "";
    content += formatted_date + _sep;
    content += "\\par \\pard \\sb300 \\plain {\\loch Dear " + _name + ",}" + _sep;
    content +=
        "\\par \\pard \\sb300 \\sa300 \\plain {\\loch your working time in "
            + start_date_str
            + " is as follows:}"
            + _sep;
    return content;
  }

  private static String formatCell(final String content) {
    return "\\intbl " + Interval.removeSpaces(content) + " \\cell" + _sep;
  }

  private String createTable() {
    String table = "\\par \\sb200 \\qc" + _sep;
    final long should_millis = _should_duration.toMillis();
    long balance = 0l;
    long total_time = 0l;
    final int[] right_bounds = new int[] {700, 1600, 2500, 3400, 4400, 5400, 6400};
    final String cell_def =
        Arrays.stream(right_bounds)
            .mapToObj(i -> "\\cellx" + Integer.toString(i))
            .reduce("", (s, a) -> s + a);
    String text_should_duration = Interval.getTextForDuration(_should_duration);

    table += "\\trowd \\trqc " + cell_def + _sep;
    table += formatCell("Date");
    table += formatCell("Start");
    table += formatCell("End");
    table += formatCell("Elapsed");
    table += formatCell("");
    table += formatCell("Sum");
    table += formatCell("Change");
    table += "\\row \\pard" + _sep;

    final Map<Long, ArrayList<Interval>> intervals_per_day = _time_manager.getGroupedIntervals();
    for (final Map.Entry<Long, ArrayList<Interval>> it : intervals_per_day.entrySet()) {
      final long elapsed_millis =
          it.getValue().stream().map(Interval::getDurationMs).mapToLong(l -> l).sum();
      String text_day_sum = Interval.getTextForDuration(Duration.ofMillis(elapsed_millis));
      String text_date = it.getValue().get(0).getDateInYear();
      String text_delta =
          (elapsed_millis >= should_millis ? "+" : "-")
              + Interval.getTextForDuration(
                  Duration.ofMillis(Math.abs(elapsed_millis - should_millis)));
      balance += (elapsed_millis - should_millis);
      total_time += elapsed_millis;

      for (final Interval interval : it.getValue()) {
        table += "\\trowd \\trqc " + cell_def + _sep;
        table += formatCell(text_date);
        table += formatCell(Interval.trimTimeInDay(interval.formatStartTime()));
        table += formatCell(Interval.trimTimeInDay(interval.formatEndTime()));
        table += formatCell(interval.getTextForDuration());
        table += formatCell(text_should_duration);
        table += formatCell(text_day_sum);
        table += formatCell(text_delta);
        table += "\\row \\pard" + _sep;

        text_date = "";
        text_should_duration = "";
        text_day_sum = "";
        text_delta = "";
      }
    }
    final String summary =
        String.format(
            "The total working time for the %d days with time tracking is %s, the balance for this period is %s.",
            intervals_per_day.size(),
            Interval.removeSpaces(Interval.getTextForDuration(Duration.ofMillis(total_time))),
            (balance >= 0l ? "+" : "-")
                + Interval.removeSpaces(
                    Interval.getTextForDuration(Duration.ofMillis(Math.abs(balance)))));
    table += "\\par \\pard \\sb300 \\plain {\\loch " + summary + "}" + _sep;
    return table;
  }

  private String createGreetingAgain() {
    String text = "\\par \\pard \\sb300 \\plain {\\loch Sincerely yours}" + _sep;
    text += "\\par \\pard \\sb300 \\plain {\\loch ProjectTimeManager from t-lou}" + _sep;
    return text;
  }

  public void output(final String filename) {
    try {
      FileOutputStream out_stream = new FileOutputStream(filename, false);
      BufferedWriter br = new BufferedWriter(new OutputStreamWriter(out_stream));

      br.write(_doc_head);
      br.newLine();
      br.newLine();
      br.write(createGreeting());
      br.newLine();
      br.write(createTable());
      br.newLine();
      br.write(createGreetingAgain());
      br.newLine();
      br.write(_doc_end);
      br.newLine();
      br.flush();

      br.close();
    } catch (Exception ex) {
    }
  }

  public static boolean isConfigReady() {
    if (!Files.exists(Paths.get(_path_config))) {
      return false;
    }
    final HashMap<String, String> config = loadConfigItems();
    return config != null && Arrays.stream(config_keys).allMatch(key -> config.containsKey(key));
  }

  public static void saveConfigItems(final HashMap<String, String> config) {
    try {
      FileOutputStream out_stream = new FileOutputStream(_path_config, false);
      BufferedWriter br = new BufferedWriter(new OutputStreamWriter(out_stream));

      for (Map.Entry<String, String> entry : config.entrySet()) {
        br.write(entry.getKey() + ": " + entry.getValue());
        br.newLine();
      }

      br.close();
    } catch (Exception ex) {
    }
  }

  public static HashMap<String, String> loadConfigItems() {
    HashMap<String, String> config = null;

    try {
      final FileInputStream in_stream = new FileInputStream(_path_config);
      final BufferedReader br = new BufferedReader(new InputStreamReader(in_stream));

      while (true) {
        String line = br.readLine();
        if (line == null || line.isEmpty()) {
          break;
        }
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

      br.close();
    } catch (Exception ex) {
      return null;
    }

    return config;
  }

  public ProjectReporter(final TimeLogManager time_manager) {
    _time_manager = time_manager;

    final HashMap<String, String> config = loadConfigItems();
    _name = config.get(_key_name);
    _should_duration = Duration.ofHours(Long.parseLong(config.get(_key_duration)));
  }
}
