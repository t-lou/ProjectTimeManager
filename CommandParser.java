import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

/// This class parses the command and input data.
public class CommandParser {
    /**
     * Print help information.
     */
    public static void printHelp() {
        System.out.println("This software helps to record the running time for for one project.");
        System.out.println();
        System.out.println("list");
        System.out.println("\tList all on-going projects with elapsed time.");
        System.out.println("list PROJECT_NAME");
        System.out.println("\tList all logs of wanted project, grouped with the starting date of each log.");
        System.out.println("start PROJECT_NAME");
        System.out.println("\tStart one project. If this project is not found, a confirmation is needed.");
    }

    /**
     * Get a text which represents the duration in hour, minute and second.
     *
     * @param duration Duration to show.
     * @return Text which shows the duration.
     */
    public static String getTextForDuration(Duration duration) {
        final String text = String.format("%3dhr %2dmin %2ds",
                duration.toHours(),
                duration.toMinutes() - duration.toHours() * 60L,
                duration.toMillis() / 1000L - duration.toMinutes() * 60L);

        return text;
    }

    /**
     * List all on-going projects and elapsed time.
     */
    public static void listProjects() {
        final ProjectManager pm = new ProjectManager();
        System.out.println("The recorded projects:");
        for (final String project_name : pm.getListProject()) {
            final Duration duration = Duration.ofMillis(new ProjectManager(project_name).getTotalTimeMs());

            System.out.println(project_name);
            System.out.println("\t" + getTextForDuration(duration));
        }
    }

    /**
     * List the logs of one project.
     *
     * @param project_name Name of the project.
     */
    public static void listProjectLog(String project_name) {
        final ProjectManager pm = new ProjectManager(project_name);
        final HashMap<Long, ArrayList<Interval>> grouped_log = pm.getGroupedLog();

        ArrayList<Long> keys = new ArrayList<>(grouped_log.keySet());
        Collections.sort(keys);

        for (final Long day : keys) {
            final String full_text_date = Instant.ofEpochSecond(day * 24L * 3600L).toString();

            final long duration_ms = grouped_log.get(day).stream().map(Interval::getDurationMs)
                    .mapToLong(l -> l).sum();

            System.out.println(full_text_date.split("T")[0] +
                    "\tElapsed time: " + getTextForDuration(Duration.ofMillis(duration_ms)));

            for (Interval interval : grouped_log.get(day)) {
                System.out.println("\t" + interval.formatDateTime());
            }
        }
    }

    /**
     * List the logs for all given projects.
     *
     * @param project_names The list of project names.
     */
    public static void listProjectsLog(String[] project_names) {
        for (final String name : project_names) {
            assert ProjectManager.isProjectAvailable(name) : "Project " + name + " not found.";
            System.out.println("--------------------------------------");

            System.out.println(name);
            listProjectLog(name);

            System.out.println("--------------------------------------");
        }
    }

    /**
     * Checks whether all given projects are available.
     *
     * @param project_names The list of project names.
     * @return Whether all given projects are available.
     */
    public static boolean areProjectsAllAvailable(String[] project_names) {
        return !Arrays.stream(project_names).map(ProjectManager::isProjectAvailable)
                .collect(Collectors.toList()).contains(false);
    }

    /**
     * Start one project. If this project is new, then a confirmation is needed.
     *
     * @param project_name The name of project to start.
     */
    public static void startProject(String project_name) {
        ProjectManager project = new ProjectManager(project_name);
        project.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println();
                System.out.println("Project " + project_name + " ends at " + LocalDateTime.now().toString());
                project.end();
            }
        });

        while (true) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException ex) {
                System.out.println("Sleep interrupted! Check date time!");
            }
        }
    }

    private static void startGui() {
        new GuiWin();
    }

    /**
     * Parse the console command.
     *
     * @param command All parameters from console.
     */
    public static void parseCommand(String[] command) {
        switch (command[0]) {
            case "start": {
                if (command.length == 2) {
                    String project_name = command[1];
                    if (ProjectManager.isProjectAvailable(project_name)) {
                        startProject(project_name);
                    } else {
                        System.out.println("Project " + project_name + " not found, create now? (y/yes to continue)");

                        final String input = System.console().readLine();
                        if (input.equals("yes") || input.equals("y")) {
                            startProject(project_name);
                        }
                    }
                } else {
                    printHelp();
                }
                break;
            }
            case "list": {
                if (command.length == 1) {
                    listProjects();
                } else if ((command.length == 2) && command[1].equals("*")) {
                    listProjectsLog(new ProjectManager().getListProject().toArray(new String[1]));
                } else {
                    final String[] project_names = Arrays.copyOfRange(command, 1, command.length);
                    if (areProjectsAllAvailable(project_names)) {
                        listProjectsLog(project_names);
                    } else {
                        System.out.println("Not all given names are found, the project include:");
                        listProjects();
                    }
                }
                break;
            }
            case "delete": {
                final String[] project_names = Arrays.copyOfRange(command, 1, command.length);

                if (areProjectsAllAvailable(project_names)) {
                    for (final String name : project_names) {
                        ProjectManager.deleteProject(name);
                    }
                } else {
                    System.out.println("Not all given names are found, the project include:");
                    listProjects();
                }
                break;
            }
            case "x": {
                startGui();
                break;
            }
            default: {
                printHelp();
                break;
            }
        }
    }
}
