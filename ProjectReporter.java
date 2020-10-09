import java.io.*;

public class ProjectReporter {

  private final TimeLogManager _time_manager;

  private final String _doc_head = "{\\rtf1\\ansi\\deff0";
  private final String _doc_end = "}";
  private final String _sep = System.lineSeparator();

  private String createTable() {
    String table = "";
    for (final Interval interval : _time_manager.getIntervals()) {
      table +=
          "\\trowd" + _sep + "\\cellx3000" + _sep + "\\cellx6000" + _sep + "\\cellx9000" + _sep;
      table += "\\intbl " + interval.getDateInYear() + "\\cell" + _sep;
      table += "\\intbl " + interval.formatStartTime() + "\\cell" + _sep;
      table += "\\intbl " + interval.formatEndTime() + "\\cell" + _sep;
      table += "\\row" + _sep;
    }
    return table;
  }

  public void output(final String filename) {
    try {
      FileOutputStream out_stream = new FileOutputStream(filename, false);
      BufferedWriter br = new BufferedWriter(new OutputStreamWriter(out_stream));

      br.write(_doc_head);
      br.newLine();
      br.write(createTable());
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
