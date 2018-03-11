package ptm;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectManager {
    /**
     * The path of the directory for storing the data.
     */
    private String _cache_path = ".ptm_projects";

    /**
     * The extension for logging data.
     */
    private String _extension = ".prt";

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
    private TimeLogManager _log_manager = new TimeLogManager();

    /**
     * The logger for this class.
     */
    private static final Logger _log = Logger.getLogger("ptm");

    /**
     * Checks whether the save data for this project exists.
     *
     * @return Whether the save data for this project exists.
     */
    private boolean isProjectAvailable() {
        return Files.exists(Paths.get(_filename));
    }

    /**
     * Get the filename for the logged time.
     *
     * @param project_name The name of this project.
     * @return
     */
    private String getLogFilename(String project_name) {
        return Paths.get(_cache_path, _name + _extension).toString();
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
            _log.log(Level.INFO, "Project " + project_name + " exists.");

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
    public void deleteProject(String project_name) {
        String filename = getLogFilename(project_name);
        if (Files.exists(Paths.get(filename))) {
            _log.log(Level.INFO, "Remove data for " + project_name + ".");
            new File(filename).delete();
        } else {
            _log.log(Level.SEVERE, "Project " + project_name + " does not exist, will not delete.");
        }
    }

    public ProjectManager() {
        // if the directory for data is not availalble, create the directory.
        File file_cache_dir = new File(_cache_path);
        if (!file_cache_dir.exists()) {
            file_cache_dir.mkdirs();
        }
    }

    public ProjectManager(String project_name) {
        this();
        initProject(project_name);
    }
}
