import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class MainCheck {
  public static void main(String[] args) {
    assert Files.exists(Paths.get(".ptm_projects", "dummy.prt")) : "project not created";

    ProjectTimeManager.TimeLogManager log_manager = new ProjectTimeManager.TimeLogManager();
    log_manager.readLog(Paths.get(".ptm_projects", "dummy.prt").toString());
    assert Math.abs(log_manager.getTotalTimeMs(null) - 1000l) < 10l : "time is wrong";

    final HashMap<Long, ArrayList<ProjectTimeManager.Interval>> grouped_intervals =
        log_manager.getGroupedIntervals();
    assert grouped_intervals.size() == 1 : "should contain one interval";
  }
}
