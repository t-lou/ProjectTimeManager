import java.io.*;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Arrays;
import java.util.List;


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
    private LocalDateTime _time_start;

    /**
     * The number of seconds in one day.
     */
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
            String text = new Interval(_time_start, LocalDateTime.now()).formatDateTime();
            br.write(text);
            br.newLine();
            br.flush();

            br.close();

            _log.info("Wrote " + _time_entries.size() + " time entries.");

            // this file is supposed to be finished, clear the cached timestamps
            _time_entries.clear();

        } catch (Exception ex) {
            _log.severe(ex.getMessage());
        }

    }

    /**
     * Checks whether the time logs are ordered, incl. whehther all intervals have later end time and whether the
     * starting time of one interval is later than the ending time of the earlier ones.
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

            return IntStream.range(1, all_time_ms.size()).
                    allMatch(i -> all_time_ms.get(i) > all_time_ms.get(i - 1));
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

        final File file = new File(filename);

        if (file.canRead()) {
            // file is available, try to read and parse
            try {
                final FileInputStream in_stream = new FileInputStream(file);
                final BufferedReader br = new BufferedReader(new InputStreamReader(in_stream));

                final List<String> ends = Arrays.asList(new String[]{"", "\n", null});
                while (true) {
                    final String line = br.readLine();

                    if (ends.contains(line)) {
                        break;
                    }

                    _time_entries.add(new Interval(line));
                }

                br.close();
            } catch (Exception ex) {
                // somewhere problem occurs
                _log.severe(ex.getMessage());
                return false;
            }

            if (!areLogsOrdered()) {
                _log.severe("Logs are not aligned.");
            }

            return true;
        } else {
            // file not available
            _log.severe("Logging file " + filename + " not found.");
            return false;
        }
    }

    /**
     * Add current timestamp to the cache list.
     */
    public void addNow() {
        _time_start = LocalDateTime.now();
        // record this timestamp
        _log.info("Added now as starting time " + _time_start.toString());
    }

    /**
     * Check whether interval is on one of the given dates. If dates is not given, then return false;
     *
     * @param interval The interval to check.
     * @param dates    The list of dates.
     * @return Whether the dates are given and the interval is on one of the dates.
     */
    public static boolean isIntervalOnDates(Interval interval, ArrayList<Instant> dates) {
        if (dates == null || dates.isEmpty()) {
            return false;
        } else {
            return dates.stream()
                    .map(Instant::getEpochSecond)
                    .collect(Collectors.toList())
                    .contains(getSecondStartOfDay(interval.getStartTime()));
        }
    }

    /**
     * Get the total elapsed time of all logped intervals in millisecond.
     *
     * @return The total elapsed time of all logped intervals in millisecond.
     */
    public long getTotalTimeMs(ArrayList<Instant> dates) {
        if (dates == null || dates.isEmpty()) {
            return _time_entries.stream()
                    .map(Interval::getDurationMs)
                    .mapToLong(l -> l)
                    .sum();
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

    public ArrayList<Interval> getIntervals() {
        return _time_entries;
    }

    public LocalDateTime getStartTime() {
        return _time_start;
    }

    static public String getCurrnetMonthId() {
        return Interval.getMonthId(LocalDateTime.now());
    }

    public TimeLogManager(Logger log) {
        _log = log;
    }
}
