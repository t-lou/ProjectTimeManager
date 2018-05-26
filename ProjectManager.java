import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public ArrayList<String> getListProject() {
        ArrayList<String> filenames = new ArrayList<String>();
        for (final File file : new File(_cache_path).listFiles()) {
            String filename = file.getName();
            int pos_point = filename.lastIndexOf(".");
            filenames.add(filename.substring(0, pos_point));
        }

        Collections.sort(filenames);

        return filenames;
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
    public long getTotalTimeMs() {
        return _log_manager.getTotalTimeMs();
    }

    /**
     * Checks whether another project is running.
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

    public ProjectManager() {
        // if the directory for data is not availalble, create the directory.
        final File file_cache_dir = new File(_cache_path);
        if (!file_cache_dir.exists()) {
            file_cache_dir.mkdirs();
        }
    }

    public ProjectManager(String project_name) {
        this();
        initProject(project_name);
    }
}