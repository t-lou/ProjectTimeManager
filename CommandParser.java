import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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
     * List all on-going projects and elapsed time.
     */
    public static void listProjects() {
        ProjectManager pm = new ProjectManager();
        System.out.println("The recorded projects:");
        for (String project_name : pm.getListProject()) {
            Duration duration = Duration.ofMillis(new ProjectManager(project_name).getTotalTimeMs());
            String text = String.format("%3d hr %2d min %2d s",
                    duration.toHours(),
                    duration.toMinutes() - duration.toHours() * 60L,
                    duration.toMillis() / 1000L - duration.toMinutes() * 60L);
            System.out.println(project_name);
            System.out.println("\t" + text);
        }
    }

    /**
     * List the logs of one project.
     *
     * @param project_name Name of the project.
     */
    public static void listProjectLog(String project_name) {
        ProjectManager pm = new ProjectManager(project_name);
        HashMap<Long, ArrayList<Interval>> grouped_log = pm.getGroupedLog();

        ArrayList<Long> keys = new ArrayList<>();
        keys.addAll(grouped_log.keySet());
        Collections.sort(keys);

        for (Long day : keys) {
            final String full_text_date = Instant.ofEpochSecond(day.longValue() * 24L * 3600L).toString();

            System.out.println(full_text_date.split("T")[0]);
            for (Interval interval : grouped_log.get(day)) {
                System.out.println("\t" + interval.formatDateTime());
            }
        }
    }

    /**
     * Start one project. If this project is new, then a confirmation is needed.
     *
     * @param project_name The name of project to start.
     */
    private static void startProject(String project_name) {
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
                Thread.sleep(500L);
            } catch (InterruptedException ex) {
                System.out.println("Sleep interrupted! Check date time!");
            }
        }
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
                        String input = System.console().readLine();
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
                } else if (command.length == 2 && ProjectManager.isProjectAvailable(command[1])) {
                    listProjectLog(command[1]);
                } else {
                    printHelp();
                }
                break;
            }
            default: {
                printHelp();
            }
        }
    }
}
