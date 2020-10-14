package ProjectTimeManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/// This class handles the projects.
public class ProjectManager {
  /** The path of the directory for storing the data. */
  private static final String _cache_path = ".ptm_projects";

  /** The extension for logging data. */
  private static final String _extension = ".prt";

  /** The name of file for this project, if the Manager is used to manage one project. */
  private String _filename;

  /** The name of the project, will be used as filename for saving the */
  private String _name;

  /** The log manager for time used in this project. */
  private TimeLogManager _log_manager = new TimeLogManager();

  private Semaphore _mutex = new Semaphore(1);

  private String getPathLock() {
    return Paths.get(_cache_path, _name + ".lk").toString();
  }

  /**
   * Checks whether the save data for this project exists.
   *
   * @return Whether the save data for this project exists.
   */
  private boolean isProjectAvailable() {
    return Files.exists(Paths.get(_filename));
  }

  /** Checks the existence directory for data and create if not exists. */
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
      _log_manager.readLog(_filename);
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
        .filter(
            filename ->
                filename.lastIndexOf(_extension) == (filename.length() - _extension.length()))
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
        .filter(
            project_name ->
                new ProjectManager(project_name).getGroupedLog().keySet().contains(time_start))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Get all available dates.
   *
   * @return List of available dates.
   */
  public static ArrayList<Instant> getListDates() {
    return new ArrayList<>(
        getListProject().stream()
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
  public boolean isRunning() {
    return Files.exists(Paths.get(getPathLock()));
  }

  public static void updatePendingSessionTime(final String project_name, final String text) {
    TimeLogManager.updateThisSession(new ProjectManager(project_name).getPathLock(), text);
  }

  public static String getPendingSessionTime(final String project_name) {
    final List<String> contents =
        TimeLogManager.readFile(new ProjectManager(project_name).getPathLock());
    assert contents != null && contents.size() == 1;
    return contents.get(0);
  }

  /** Start this project. */
  public void start() {
    if (isRunning()) {
      System.out.println("Another project is running.");
      System.exit(1);
    }

    _log_manager.addNow();
    try {
      updateThisSession();
    } catch (Exception ex) {
      System.out.println("Cannot establish lock file, thus cannot start.");
      System.exit(1);
    }
  }

  /** End this project and log the time. */
  public void end() {
    assert (isRunning());

    try {
      _mutex.acquire();
      _log_manager.updateLog(_filename);
    } catch (Exception er) {
    } finally {
      _mutex.release();
    }

    deleteLock();
  }

  /** Get the log manager to access logged intervals. */
  public TimeLogManager getLogManager() {
    return _log_manager;
  }

  public void updateThisSession() {
    try {
      _mutex.acquire();
      _log_manager.updateThisSession(getPathLock());
    } catch (Exception er) {
    } finally {
      _mutex.release();
    }
  }

  public ProjectManager() {
    prepareDirectory();
  }

  public ProjectManager(String project_name) {
    this();
    initProject(project_name);
  }

  public void deleteLock() {
    new File(getPathLock()).delete();
  }

  public static void finishLastSession(final String project_name) {
    ProjectManager project = new ProjectManager(project_name);
    if (project.isRunning()) {
      project.getLogManager().addLog(project.getPathLock());
      project.end();
    }
  }

  /**
   * Start one project. If this project is new, then a confirmation is needed.
   *
   * @param project_name The name of project to start.
   */
  public static void startProject(String project_name) {
    ProjectManager project = new ProjectManager(project_name);
    project.start();
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              public void run() {
                System.out.println(
                    "Project " + project_name + " ends at " + LocalDateTime.now().toString());
                project._log_manager.closeNow();
                project.end();
              }
            });

    while (project.isRunning()) {
      try {
        Thread.sleep(300l * 1000l);
        project.updateThisSession();
      } catch (InterruptedException ex) {
        System.out.println("Sleep interrupted! Check date time!");
      }
    }
  }
}
