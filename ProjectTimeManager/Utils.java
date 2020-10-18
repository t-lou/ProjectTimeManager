package ProjectTimeManager;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Utils {

  public static List<String> readFile(final String filename) {
    List<String> contents = null;
    final File file = new File(filename);
    if (file.canRead()) {
      try {
        final FileInputStream in_stream = new FileInputStream(file);
        final BufferedReader br = new BufferedReader(new InputStreamReader(in_stream));

        contents = new LinkedList<String>();
        final List<String> ends = Arrays.asList(new String[] {"", "\n", "\r", null});
        while (true) {
          final String line = br.readLine();
          if (ends.contains(line)) {
            break;
          }
          contents.add(line);
        }

        br.close();
      } catch (Exception ex) {
        assert 1 == 2 : ("error loading intervals from " + filename);
      }
    }
    return contents;
  }

  public static int countFileLine(final String filename) {
    final List<String> content = readFile(filename);
    return content == null ? 0 : content.size();
  }

  public static boolean writeFile(final String filename, final List<String> contents) {
    File file = new File(filename);
    try {
      FileOutputStream out_stream = new FileOutputStream(file, false);
      BufferedWriter br = new BufferedWriter(new OutputStreamWriter(out_stream));
      for (final String content : contents) {
        br.write(content);
        br.newLine();
      }
      br.flush();

      br.close();
    } catch (Exception ex) {
      assert 1 == 2 : ("error writing intervals to " + filename);
      return false;
    }
    return true;
  }
}
