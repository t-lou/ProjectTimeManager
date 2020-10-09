import java.io.*;

public class ProjectReporter {

    final private TimeLogManager _time_manager;

    private final String _doc_head = "{\\rtf1\\ansi\\deff0";
    private final String _doc_end = "}";

    private String createTable() {
        String table = "";
        for (final Interval interval : _time_manager.getIntervals()) {
            table += "\\trowd" + System.lineSeparator() + "\\cellx3000" + System.lineSeparator() + "\\cellx6000" + System.lineSeparator() + "\\cellx9000" + System.lineSeparator();
            table += "\\intbl " + interval.getDateInYear() + "\\cell" + System.lineSeparator();
            table += "\\intbl " + interval.formatStartTime() + "\\cell" + System.lineSeparator();
            table += "\\intbl " + interval.formatEndTime() + "\\cell" + System.lineSeparator();
            table += "\\row" + System.lineSeparator();
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