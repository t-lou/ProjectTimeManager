package ptm;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimeLogManager {
    /**
     * The pattern for saving the timestamp in text.
     */
    static private String _pattern = "dd/MM/yyyy HH:mm:ss";

    /**
     * The formatter between timestamp and text.
     */
    static private DateTimeFormatter _formatter = DateTimeFormatter.ofPattern(_pattern);

    /**
     * The list of cached timestamps.
     */
    private ArrayList<LocalDateTime> _time_entries = new ArrayList<LocalDateTime>();

    /**
     * The logger for this class.
     */
    private static final Logger _log = Logger.getLogger("ptm");

    /**
     * Read and parse the timestamp from the given text.
     *
     * @param text The text to read.
     * @return The timestamp represented in the given text.
     */
    private LocalDateTime parseDateTime(String text) {
        return LocalDateTime.parse(text, _formatter);
    }

    /**
     * Return the text which represents the given timestamp.
     *
     * @param data_time The timestamp to represent.
     * @return The text for this timestamp.
     */
    private String formatDateTime(LocalDateTime data_time) {
        return data_time.format(_formatter);
    }

    /**
     * Write the recorded time entries to file with given filename.
     *
     * @param filename The given filename.
     */
    public void writeLog(String filename) {
        if (!_time_entries.isEmpty() && (_time_entries.size() % 2) == 0) {
            // the number of the cached timestamps should be correct, open file and append
            File file = new File(filename);
            try {

                FileOutputStream out_stream = new FileOutputStream(file, true);

                BufferedWriter br = new BufferedWriter(new OutputStreamWriter(out_stream));

                for (LocalDateTime time_entry : _time_entries) {
                    // for each timestamp, write the text for it and write to file
                    br.write(formatDateTime(time_entry));
                    br.newLine();
                    br.flush();
                }

                br.close();

                _log.log(Level.INFO, "Wrote " + _time_entries.size() + " time entries.");

                // this file is supposed to be finished, clear the cached timestamps
                _time_entries.clear();

            } catch (Exception ex) {
                _log.log(Level.SEVERE, ex.getMessage());
            }
        } else {
            // the number of the cached timestamps is not right
            _log.log(Level.WARNING, "The number of time entries is odd or zero.");
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

                String line = null;
                while ((line = br.readLine()) != null) {
                    _time_entries.add(parseDateTime(line));
                }

                br.close();
            } catch (Exception ex) {
                // somewhere problem occurs
                _log.log(Level.SEVERE, ex.getMessage());
                return false;
            }

            if (_time_entries.size() % 2 != 0) {
                // the number of the timestamp entries is not right (should be even)
                _time_entries.clear();
                _log.log(Level.SEVERE, "Logging file " + filename + " contains odd number of entries.");
                return false;
            } else {
                _log.log(Level.INFO, "Read " + _time_entries.size() + " time entries.");
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
        LocalDateTime now = LocalDateTime.now();
        // record this timestamp
        _log.log(Level.INFO, "Added time " + formatDateTime(now));
        // add the current timestamp to cached list of timestamps
        _time_entries.add(now);
    }
}
