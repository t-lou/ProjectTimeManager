import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/// This class handles the projects.
public class ProjectManager {
    /**
     * The path of the directory for storing the data.
     */
    final private static String _cache_path = ".ptm_projects";

    /**
     * The extension for logging data.
     */
    final private static String _extension = ".prt";

    final private static String _path_lock = Paths.get(_cache_path, "lock.lk").toString();

    /**
     * The name of file for this project, if the Manager is used to manage one project.
     */
    private String _filename;

    /**
     * The name of the project, will be used as filename for saving the
     */
    private String _name;

    /**
     * The log manager for time used in this project.
     */
    private TimeLogManager _log_manager = new TimeLogManager(_log);

    /**
     * The logger for this class.
     */
    private static Logger _log = Logger.getLogger("ptm");

    /**
     * Checks whether the save data for this project exists.
     *
     * @return Whether the save data for this project exists.
     */
    private boolean isProjectAvailable() {
        return Files.exists(Paths.get(_filename));
    }

    /**
     * Checks the existence directory for data and create if not exists.
     */
    private static void prepareDirectory() {
        // if the directory for data is not availalble, create the directory.
        final File file_cache_dir = new File(_cache_path);
        if (!file_cache_dir.exists()) {
            file_cache_dir.mkdirs();
        }
    }

    /**
     * Checks whether the project with given name is available.
     *
     * @param project_name The name of consulted project.
     * @return Whether the project with given name is available.
     */
    public static boolean isProjectAvailable(String project_name) {
        return Files.exists(Paths.get(getLogFilename(project_name)));
    }

    /**
     * Get the filename for the logged time.
     *
     * @param project_name The name of this project.
     * @return
     */
    private static String getLogFilename(String project_name) {
        return Paths.get(_cache_path, project_name + _extension).toString();
    }

    /**
     * Initialize one project, read the previous logging time.
     *
     * @param project_name The name of this project.
     */
    private void initProject(String project_name) {
        _name = project_name;

        _filename = getLogFilename(project_name);

        if (isProjectAvailable()) {
            _log.log(Level.FINE, "Project " + project_name + " exists.");

            _log_manager.readLog(_filename);
        } else {
            _log.log(Level.INFO, "Project " + project_name + " does not exist.");
        }
    }

    /**
     * Get the available projects.
     *
     * @return A list of the available projects.
     */
    public static ArrayList<String> getListProject() {
        prepareDirectory();

        return Arrays.stream(new File(_cache_path).listFiles())
                .map(File::getName)
                .filter(filename -> filename.lastIndexOf(_extension) == (filename.length() - _extension.length()))
                .map(filename -> filename.substring(0, filename.lastIndexOf(_extension)))
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get the projects which involve given date.
     *
     * @return A list of projects which involve given date.
     */
    public static ArrayList<String> getListProjectWithData(Instant date) {
        final Long time_start = date.getEpochSecond() / TimeLogManager.SECONDS_PER_DAY;

        return getListProject().stream()
                .filter(project_name -> new ProjectManager(project_name).getGroupedLog().keySet().contains(time_start))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get all available dates.
     *
     * @return List of available dates.
     */
    public static ArrayList<Instant> getListDates() {
        return new ArrayList<>(getListProject().stream()
                .map(project_name -> new ProjectManager(project_name).getGroupedLog().keySet())
                .flatMap(set -> set.stream())
                .distinct()
                .sorted(Collections.reverseOrder())
                .map(date -> Instant.ofEpochSecond(date * TimeLogManager.SECONDS_PER_DAY))
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    /**
     * Delete the data for one project if it exists; otherwise just give a warning.
     *
     * @param project_name The name of this project.
     */
    public static void deleteProject(String project_name) {
        assert isProjectAvailable(project_name) : "Project " + project_name + " not found.";

        new File(getLogFilename(project_name)).delete();
    }

    /**
     * Get the logs grouped by date (from Posix epoch) of starting time.
     *
     * @return A map with date (from Posix epoch) as key and list of logs as values.
     */
    public HashMap<Long, ArrayList<Interval>> getGroupedLog() {
        return _log_manager.getGroupedIntervals();
    }

    /**
     * Get the total time of this project in millisecond.
     *
     * @return Total time of this project in millisecond.
     */
    public long getTotalTimeMs(ArrayList<Instant> dates) {
        return _log_manager.getTotalTimeMs(dates);
    }

    /**
     * Checks whether another project is running.
     *
     * @return Whether another project is running.
     */
    private static boolean isRunning() {
        return Files.exists(Paths.get(_path_lock));
    }

    /**
     * Start this project.
     */
    public void start() {
        if (isRunning()) {
            System.out.println("Another project is running.");
            System.exit(1);
        }

        try {
            new File(_path_lock).createNewFile();
        } catch (Exception ex) {
            _log.severe("Cannot establish lock file, thus cannot start.");
            System.exit(1);
        }
        _log_manager.addNow();
    }

    /**
     * End this project and log the time.
     */
    public void end() {
        if (!isRunning()) {
            _log.severe("No project is running. ");
            _log.severe("Start time: " + _log_manager.getStartTime().toString());
            _log.severe("End time:   " + LocalDateTime.now().toString());
            System.exit(1);
        }

        _log_manager.updateLog(_filename);

        new File(_path_lock).delete();
    }

  public TimeLogManager getLogManager() {
        return _log_manager;
    }

    public ProjectManager() {
        prepareDirectory();
    }

    public ProjectManager(String project_name) {
        this();
        initProject(project_name);
    }
}
