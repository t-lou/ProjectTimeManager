import java.time.Duration;
import java.io.*;
import java.util.Arrays;
import java.time.LocalDateTime;

public class ProjectReporter {

  private final TimeLogManager _time_manager;

  private final String _doc_head = "{\\rtf1\\ansi\\deff0";
  private final String _doc_end = "}";
  private final String _sep = System.lineSeparator();
  private final String _name = "Max Mustermann";
  private final Duration _should_duration = Duration.ofHours(8);

  private String createGreeting() {
    final String start_date_str =
        Interval.getMonthAndYear(_time_manager.getIntervals().get(0).getStartTime());
    final String formatted_date =
        String.format("\\qr {\\loch %s}", Interval.getDate(LocalDateTime.now()));
    String content = "";
    content += formatted_date + _sep;
    content += "\\par \\pard \\sb100 \\plain {\\loch Dear " + _name + ",}" + _sep;
    content +=
        "\\par \\pard \\sb100 \\plain {\\loch your working time in "
            + start_date_str
            + " is as follows:}"
            + _sep;
    return content;
  }

  private String createTable() {
    String table = "\\par \\pard \\sb100 \\qc" + _sep;
    String last_date = "";
    final int[] right_bounds = new int[] {1000, 3500, 6000, 7000, 8000};
    final String cell_def =
        Arrays.stream(right_bounds)
            .mapToObj(i -> "\\cellx" + Integer.toString(i))
            .reduce("", (s, a) -> s + a);
    for (final Interval interval : _time_manager.getIntervals()) {
      final String this_date = interval.getDateInYear();
      table += "\\trowd\\trgaph10\\trqc\\trpaddl10\\trpaddr10\\trpaddfl3\\trpaddfr3" + _sep;
      table += cell_def + _sep;
      table += "\\intbl " + (this_date == last_date ? "" : this_date) + " \\cell" + _sep;
      table += "\\intbl " + interval.formatStartTime() + " \\cell" + _sep;
      table += "\\intbl " + interval.formatEndTime() + " \\cell" + _sep;
      table += "\\intbl " + interval.getTextForDuration() + " \\cell" + _sep;
      table += "\\intbl " + Interval.getTextForDuration(_should_duration) + " \\cell" + _sep;
      table += "\\row" + _sep;

      last_date = this_date;
    }
    table += "\\par" + _sep;
    return table;
  }

  private String createGreetingAgain() {
    return "Sincerely your" + _sep + "ProjectTimeManager from t-lou";
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

  public ProjectReporter(final TimeLogManager time_manager) {
    _time_manager = time_manager;
  }
}
