package ptm;

import java.time.Duration;
import java.time.LocalDateTime;

public class CommandParser {
    public static void printHelp() {
        System.out.println("This software helps to record the running time for for one project.");
        System.out.println("");
        System.out.println("list");
        System.out.println("\tList all on-going projects with elapsed time.");
    }

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

    private static void startProject(String project_name) {
        ProjectManager project = new ProjectManager(project_name);
        project.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("");
                System.out.println("Project " + project_name + " ends at " + LocalDateTime.now().toString());
                project.end();
            }
        });
        while (true) {
            try {
                Thread.sleep(500L);
            }
            catch (InterruptedException ex)
            {
                System.out.println("Sleep interrupted! Check date time!");
            }
        }
    }

    public static void parseCommand(String[] command) {
        switch (command[0]) {
            case "start": {
                if (command.length == 2) {
                    String project_name = command[1];
                    if (ProjectManager.isProjectAvailable(project_name)) {
                        startProject(project_name);
                    } else {
                        System.out.println("Project " + project_name + " not found, create now?");
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
                listProjects();
                break;
            }
            default: {
                printHelp();
            }
        }
    }
}
