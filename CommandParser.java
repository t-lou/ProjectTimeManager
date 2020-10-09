import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

/// This class parses the command and input data.
// TODO clean
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
        System.out.println("date");
        System.out.println("\tList the dates of all on-going projects.");
        System.out.println("x");
        System.out.println("\tStart the GUI.");
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
        System.out.println("The recorded projects:");
        for (final String project_name : ProjectManager.getListProject()) {
            final Duration duration = Duration.ofMillis(new ProjectManager(project_name).getTotalTimeMs(null));

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

        final ArrayList<Long> keys = new ArrayList<>(grouped_log.keySet()).stream().
                sorted().collect(Collectors.toCollection(ArrayList::new));

        for (final Long day : keys) {
            final String full_text_date = Instant.ofEpochSecond(day * TimeLogManager.SECONDS_PER_DAY).toString();

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
        return !Arrays.stream(project_names)
                .map(ProjectManager::isProjectAvailable)
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

    public static void checkIn() {
        startProject(TimeLogManager.getCurrnetMonthId());
    }
}
