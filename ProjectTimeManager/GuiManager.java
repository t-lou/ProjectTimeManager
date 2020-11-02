package ProjectTimeManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GuiManager {
  /** Width of one unit (button or text field). */
  private static final int _width_per_unit = 360;

  /** Height of one unit (button or text field). */
  private static final int _height_per_unit = 90;

  /** Overhead for one unit, to improve the visualization of GUI. */
  private static final int _size_overhead = 10;

  /** The colour for background. */
  private static final Color _colour_background = Color.GRAY;

  /** The colour for foreground, font. */
  private static final Color _colour_foreground = Color.WHITE;

  /** The colour for boundary. */
  private static final Color _colour_boundary = Color.LIGHT_GRAY;

  /** The object for GUI. */
  private JFrame _gui = null;

  /** The size of one unit, button or text field. */
  private static final Dimension _dimension = new Dimension(_width_per_unit, _height_per_unit);

  /**
   * Initialize button with text and predefined looks.
   *
   * @param text The text to show on the button.
   * @param action_listener Action to take when button is pressed.
   */
  private static JButton initButton(final String text, final ActionListener action_listener) {
    JButton button = new JButton(text);
    button.setBackground(_colour_background);
    button.setForeground(_colour_foreground);
    button.setPreferredSize(_dimension);
    button.addActionListener(action_listener);
    return button;
  }

  /** Close the current GUI session. */
  private void destroyGui() {
    if (_gui != null) {
      _gui.setVisible(false);
      _gui.dispose();
      _gui = null;
    }
  }

  /**
   * Clean and initilize GUI with given title and one filled panel, and return the panel.
   *
   * @param title Title of the window.
   * @return The panel to place buttons, texts and other GUI components.
   */
  private JPanel initGuiWithPanel(final String title) {
    destroyGui();

    _gui = new JFrame(title);
    _gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel panel = new JPanel();

    panel.setBackground(_colour_boundary);
    panel.setPreferredSize(new Dimension(_width_per_unit, _height_per_unit * 4));

    JScrollPane scrollable = new JScrollPane(panel);
    scrollable.setPreferredSize(
        new Dimension(
            _width_per_unit + _size_overhead * 2, _height_per_unit * 4 + _size_overhead * 3));
    _gui.add(scrollable);

    panel.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
              destroyGui();
              mainMenu();
            }
          }
        });

    return panel;
  }

  /** Show the GUI. */
  private void prepareGui() {
    _gui.pack();
    _gui.setVisible(true);
  }

  /** Show the list of all possible projects. */
  private void showProjectList() {
    showProjectList(ProjectManager.getListProject(), null);
  }

  /**
   * Show the given projects.
   *
   * @param project_names Names of the given projects.
   */
  private void showProjectList(
      final ArrayList<String> project_names, final ArrayList<Instant> dates) {
    final String title_date =
        ((dates == null || dates.isEmpty())
            ? "-"
            : (dates.stream()
                .map(date -> date.toString())
                .map(text -> text.substring(2, text.indexOf('T')))
                .reduce("", (s, t) -> (" " + s + t))));

    JPanel panel = initGuiWithPanel("List of projects" + title_date);

    for (final String project_name : project_names) {
      final String text =
          project_name
              + " "
              + Interval.formatDuration(
                  Duration.ofMillis(new ProjectManager(project_name).getTotalTimeMs(dates)));
      final JButton button =
          initButton(
              text,
              new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  showProject(project_name, dates);
                }
              });

      panel.add(button);
    }

    prepareGui();
  }

  /**
   * Show the details of one single project.
   *
   * @param project_name The name of The project.
   */
  private void showProject(final String project_name, ArrayList<Instant> preferred_dates) {
    JPanel panel = initGuiWithPanel("More on Project " + project_name);

    if (preferred_dates == null || preferred_dates.isEmpty()) {
      panel.add(
          initButton(
              "REPORT",
              new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  destroyGui();
                  if (!ProjectReporter.isConfigReady()) {
                    generateReportAfterConfig(project_name);
                  } else {
                    generateReport(project_name);
                  }
                }
              }));
      panel.add(
          initButton(
              "DELETE",
              new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  ProjectManager.deleteProject(project_name);
                  showProjectList();
                }
              }));
    }

    final String text = new ProjectManager(project_name).getProjectSummary(preferred_dates);
    JTextArea label = new JTextArea(text);

    final int num_line =
        "\n\r"
            .chars()
            .map(end -> text.chars().map(ch -> (ch == end ? 1 : 0)).sum())
            .max()
            .getAsInt();
    label.setPreferredSize(new Dimension(_width_per_unit, _height_per_unit * num_line / 6));
    // center alignment not working
    // label.setAlignmentY(JTextArea.CENTER_ALIGNMENT);
    // label.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
    panel.add(label);

    prepareGui();
  }

  private void generateReport(final String project_name) {
    final String format = "rtf";
    final String ext = "." + format;
    final JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new FileNameExtensionFilter(format, format));
    chooser.showOpenDialog(null);

    String filename = chooser.getSelectedFile().getAbsolutePath();
    if (filename.length() <= 4 || !filename.substring(filename.length() - 4).equals(ext)) {
      filename += ext;
    }

    new ProjectReporter(project_name).output(filename);
  }

  private void generateReportAfterConfig(final String project_name) {
    JPanel panel = initGuiWithPanel("Configuration for reporter");

    HashMap<String, String> pre_config = ProjectReporter.loadConfigItems();
    HashMap<String, JTextField> text_fields = new HashMap<String, JTextField>();
    for (final String item : ProjectReporter.config_keys) {
      JLabel label = new JLabel(item);
      label.setPreferredSize(new Dimension(_dimension.width, _dimension.height / 3));
      label.setHorizontalAlignment(JTextField.CENTER);
      panel.add(label);
      JTextField field = new JTextField();
      field.setPreferredSize(new Dimension(_dimension.width, _dimension.height / 3));
      field.setHorizontalAlignment(JTextField.CENTER);
      if (pre_config != null && pre_config.containsKey(item)) {
        field.setText(pre_config.get(item));
      }
      panel.add(field);
      text_fields.put(item, field);
    }
    panel.add(
        initButton(
            "GENERATE",
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                HashMap<String, String> config = new HashMap<String, String>();
                for (Map.Entry<String, JTextField> entry : text_fields.entrySet()) {
                  config.put(entry.getKey(), entry.getValue().getText());
                }

                ProjectReporter.saveConfigItems(config);

                generateReport(project_name);

                destroyGui();
              }
            }));

    prepareGui();
  }

  private void checkAndStartProject(final String project_name) {
    if (new ProjectManager(project_name).isRunning()) {
      final JButton button_delete_start =
          initButton(
              "DELETE && START",
              new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  destroyGui();
                  ProjectManager.skipPendingSessionAndStart(project_name);
                }
              });

      final JTextField field_edit_start = new JTextField();
      field_edit_start.setPreferredSize(_dimension);
      field_edit_start.setHorizontalAlignment(JTextField.CENTER);
      field_edit_start.setText(ProjectManager.getPendingSessionTime(project_name));
      field_edit_start.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              destroyGui();
              ProjectManager.finishPendingSessionAndStart(project_name, field_edit_start.getText());
            }
          });

      JPanel panel = initGuiWithPanel("Configuration for reporter");

      panel.add(button_delete_start);
      panel.add(field_edit_start);

      prepareGui();

    } else {
      ProjectManager.startProject(project_name);
    }
  }

  /** Show the main menu for starting one project, either available or new. */
  private void startProjectMenu() {
    final ArrayList<String> project_names = ProjectManager.getListProject();

    JPanel panel = initGuiWithPanel("Choose Project to Start");

    final JTextField field = new JTextField();
    field.setPreferredSize(_dimension);
    field.setHorizontalAlignment(JTextField.CENTER);

    field.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            destroyGui();

            checkAndStartProject(field.getText());
          }
        });
    panel.add(field);

    for (final String project_name : project_names) {
      final JButton button =
          initButton(
              project_name,
              new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  destroyGui();

                  checkAndStartProject(project_name);
                }
              });

      panel.add(button);
    }

    prepareGui();
  }

  /** Create the GUI for selecting the date to show. */
  private void startDateMenu(ArrayList<Instant> dates) {
    if (dates == null) {
      dates = ProjectManager.getListDates();
    }

    JPanel panel = initGuiWithPanel("Choose date to inquire");

    for (final Instant date : dates) {
      final String text_date = date.toString().split("T")[0];
      final JButton button =
          initButton(
              text_date,
              new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  final Instant date =
                      Instant.ofEpochSecond(
                          TimeLogManager.getSecondStartOfDay(
                              Interval.parseDateTime(text_date + " 00:00:00")));

                  showProjectList(
                      ProjectManager.getListProjectWithData(date),
                      new ArrayList<>(Arrays.asList(new Instant[] {date})));
                }
              });

      panel.add(button);
    }

    prepareGui();
  }

  /** Show the main menu. */
  private void mainMenu() {
    final JButton button_clockin =
        initButton(
            "CLOCK IN",
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {

                destroyGui();
                checkAndStartProject(TimeLogManager.getCurrnetMonthId());
              }
            });
    final JButton button_start =
        initButton(
            "START",
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                startProjectMenu();
              }
            });
    final JButton button_list =
        initButton(
            "PROJECTS",
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                showProjectList();
              }
            });
    final JButton button_date =
        initButton(
            "DATE",
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                startDateMenu(null);
              }
            });

    JPanel panel = initGuiWithPanel("Project Time Manager");

    panel.add(button_clockin);
    panel.add(button_start);
    panel.add(button_list);
    panel.add(button_date);

    prepareGui();
  }

  /** Create the main GUI. */
  public GuiManager() {
    mainMenu();
  }
}
