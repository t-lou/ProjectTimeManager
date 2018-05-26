import java.io.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

class Interval {
    /**
     * Starting time.
     */
    private LocalDateTime _time_start;

    /**
     * End time.
     */
    private LocalDateTime _time_end;

    /**
     * The pattern for saving the timestamp in text.
     */
    static private String _pattern = "dd/MM/yyyy HH:mm:ss";

    /**
     * The formatter between timestamp and text.
     */
    static private DateTimeFormatter _formatter = DateTimeFormatter.ofPattern(_pattern);

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
     * Return the text which represents the given timestamp.
     *
     * @return The text for this timestamp.
     */
    public String formatDateTime() {
        return _time_start.format(_formatter) + " - " + _time_end.format(_formatter);
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
     * Get the starting time.
     *
     * @return The starting time.
     */
    public LocalDateTime getStartTime() {
        return _time_start;
    }

    public Interval(String text) {
        String[] slices = text.split(" - ");

        assert slices.length == 2 : "Line in log cannot be parsed.";

        _time_start = parseDateTime(slices[0]);
        _time_end = parseDateTime(slices[1]);
    }

    public Interval(LocalDateTime time_start, LocalDateTime time_end) {
        _time_start = time_start;
        _time_end = time_end;
    }
}

public class TimeLogManager {
    /**
     * The list of cached timestamps.
     */
    private ArrayList<Interval> _time_entries = new ArrayList<>();

    /**
     * The logger for this class.
     */
    private Logger _log;

    /**
     * Starting time of the latest interval (on-going interval if this project is opened).
     */
    private LocalDateTime _now;

    /**
     * Write the recorded time entries to file with given filename.
     *
     * @param filename The given filename.
     */
    public void updateLog(String filename) {
        File file = new File(filename);
        try {

            FileOutputStream out_stream = new FileOutputStream(file, false);

            BufferedWriter br = new BufferedWriter(new OutputStreamWriter(out_stream));

            for (Interval interval : _time_entries) {
                // for each timestamp, write the text for it and write to file
                br.write(interval.formatDateTime());
                br.newLine();
                br.flush();
            }
            String text = new Interval(_now, LocalDateTime.now()).formatDateTime();
            br.write(text);
            br.newLine();
            br.flush();

            br.close();

            _log.log(Level.INFO, "Wrote " + _time_entries.size() + " time entries.");

            // this file is supposed to be finished, clear the cached timestamps
            _time_entries.clear();

        } catch (Exception ex) {
            _log.log(Level.SEVERE, ex.getMessage());
        }

    }

    /**
     * Read the time entries which are stored in the file with given filename.
     *
     * @param filename The filename of the logging file.
     * @return Whether reading the time log is successful, it is successful only if the file exists and contains
     * even number of entries.
     */
    public boolean readLog(String filename) {
        _time_entries.clear();

        File file = new File(filename);

        if (file.canRead()) {
            // file is available, try to read and parse
            try {

                FileInputStream in_stream = new FileInputStream(file);

                BufferedReader br = new BufferedReader(new InputStreamReader(in_stream));

                String line;
                while (true) {
                    line = br.readLine();

                    if (line == null || line.equals("")) {
                        break;
                    }

                    _time_entries.add(new Interval(line));
                }

                br.close();
            } catch (Exception ex) {
                // somewhere problem occurs
                _log.log(Level.SEVERE, ex.getMessage());
                return false;
            }

            return true;
        } else {
            // file not available
            _log.log(Level.SEVERE, "Logging file " + filename + " not found.");
            return false;
        }
    }

    /**
     * Add current timestamp to the cache list.
     */
    public void addNow() {
        _now = LocalDateTime.now();
        // record this timestamp
        _log.log(Level.INFO, "Added now as starting time " + _now.toString());
    }

    /**
     * Get the total elapsed time of all logped intervals in millisecond.
     *
     * @return The total elapsed time of all logped intervals in millisecond.
     */
    public long getTotalTimeMs() {
        long total_duration_ms = 0L;
        for (Interval interval : _time_entries) {
            total_duration_ms += interval.getDurationMs();
        }
        return total_duration_ms;
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

    LocalDateTime getStartTime() {
        return _now;
    }

    public TimeLogManager(Logger log) {
        _log = log;
    }
}
