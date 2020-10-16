import java.io.File;

class ThreadPTM extends Thread {
  private static void rmDir(final File dir) {
    if (dir.isDirectory()) {
      for (final String file : dir.list()) {
        rmDir(new File(dir, file));
      }
    }
    dir.delete();
  }

  public void run() {
    rmDir(new File(".ptm_projects"));
    System.out.println("will start PTM");
    ProjectTimeManager.ProjectManager.startProject("dummy");
  }
}

public class Main {
  public static void main(String[] args) {
    new ThreadPTM().start();
    try {
      Thread.sleep(1000l);
    } catch (Exception ex) {
      assert 0 == 1;
    }
    System.exit(0);
  }
}
