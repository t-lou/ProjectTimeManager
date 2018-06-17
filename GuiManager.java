import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class GuiManager {
    /**
     * Width of one unit (button or text field).
     */
    final static private int _width_per_unit = 400;

    /**
     * Height of one unit (button or text field).
     */
    final static private int _height_per_unit = 100;

    /**
     * Overhead for one unit, to improve the visualization of GUI.
     */
    final static private int _size_overhead = 10;

    /**
     * The colour for background.
     */
    final static private Color _colour_background = Color.GRAY;

    /**
     * The colour for foreground, font.
     */
    final static private Color _colour_foreground = Color.WHITE;

    /**
     * The colour for boundary.
     */
    final static private Color _colour_boundary = Color.LIGHT_GRAY;

    /**
     * The object for GUI.
     */
    private JFrame _gui;

    /**
     * The size of one unit, button or text field.
     */
    final static private Dimension _dimension = new Dimension(
            _width_per_unit,
            _height_per_unit - _size_overhead);

    /**
     * The size of the whole GUI.
     */
    final static private Dimension _dimension_gui = new Dimension(
            _width_per_unit + _size_overhead * 2,
            _height_per_unit * 5 - _size_overhead * 2);

    /**
     * Set the colour of button, incl. background and foreground (font).
     *
     * @param button The button to paint.
     */
    static private void setButtonColour(final JButton button) {
        button.setBackground(_colour_background);
        button.setForeground(_colour_foreground);
    }

    /**
     * Close the current GUI session.
     */
    private void destroyGui() {
        _gui.setVisible(false);
        _gui.dispose();
        _gui = null;
    }

    /**
     * Add panel to a scrollable panel, after that the GUI contains only this panel for simplicity.
     *
     * @param panel The panel to add.
     */
    private void addPanelToGui(final JPanel panel) {
        final JScrollPane scrollable = new JScrollPane(panel);
        scrollable.setPreferredSize(_dimension_gui);
        _gui.add(scrollable);
    }

    /**
     * Show the GUI.
     */
    private void prepareGui() {
        _gui.pack();
        _gui.setVisible(true);
    }

    /**
     * Show the list of all possible projects.
     */
    private void showProjectList() {
        showProjectList(ProjectManager.getListProject(), null);
    }

    /**
     * Add mouse action: right click the empty space and return to root menu.
     *
     * @param panel The panel to change.
     */
    private void addRightButtonReturn(final JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    destroyGui();
                    mainMenu();
                }
            }
        });
    }

    /**
     * Show the given projects.
     *
     * @param project_names Names of the given projects.
     */
    private void showProjectList(final ArrayList<String> project_names, final ArrayList<Instant> dates) {
        if (_gui != null) {
            destroyGui();
        }

        _gui = new JFrame("List of Projects");
        _gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel panel = new JPanel();

        panel.setBackground(_colour_boundary);
        panel.setPreferredSize(new Dimension(_width_per_unit, _height_per_unit * project_names.size()));

        for (final String project_name : project_names) {
            final String text = project_name + ": " + CommandParser.getTextForDuration(
                    Duration.ofMillis(new ProjectManager(project_name).getTotalTimeMs(dates)));
            final JButton button = new JButton(text);

            setButtonColour(button);
            button.setPreferredSize(_dimension);

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showProject(project_name, dates);
                }
            });

            panel.add(button);
        }

        addRightButtonReturn(panel);
        addPanelToGui(panel);

        prepareGui();
    }

    /**
     * Show the details of one single project.
     *
     * @param project_name The name of The project.
     */
    private void showProject(final String project_name, ArrayList<Instant> preferred_dates) {
        if (_gui != null) {
            destroyGui();
        }

        _gui = new JFrame("More on Project " + project_name);
        _gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel panel = new JPanel();

        panel.setBackground(_colour_boundary);

        // todo check the existance of project.
        final HashMap<Long, ArrayList<Interval>> grouped_log = new ProjectManager(project_name).getGroupedLog();

        final ArrayList<Instant> dates = (preferred_dates != null) ? preferred_dates : ProjectManager.getListDates();

        final ArrayList<Long> dates_as_key = dates.stream()
                .map(date -> (date.getEpochSecond() / TimeLogManager.SECONDS_PER_DAY))
                .collect(Collectors.toCollection(ArrayList::new));

        final ArrayList<Long> keys = new ArrayList<>(grouped_log.keySet()).stream()
                .filter(date -> dates_as_key.contains(date))
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));

        final String eol = "<br/>";
        String text = "<html>";

        for (final Long day : keys) {
            text += Instant.ofEpochSecond(day * TimeLogManager.SECONDS_PER_DAY).toString().split("T")[0];

            final long duration_ms = grouped_log.get(day).stream().map(Interval::getDurationMs)
                    .mapToLong(l -> l).sum();

            text += ": " + CommandParser.getTextForDuration(Duration.ofMillis(duration_ms)) + eol;

            text += String.join(eol,
                    grouped_log.get(day).stream()
                            .map(interval -> interval.formatDateTime())
                            .collect(Collectors.toList()));

            text += eol + eol;
        }

        text += "</html>";

        panel.add(new JLabel(text));

        addRightButtonReturn(panel);
        addPanelToGui(panel);

        prepareGui();
    }

    /**
     * Show the main manu for starting one project, either available or new.
     */
    private void startProjectMenu() {
        if (_gui != null) {
            destroyGui();
        }

        final ArrayList<String> project_names = ProjectManager.getListProject();

        _gui = new JFrame("Choose Project to Start");
        _gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel panel = new JPanel();
        panel.setBackground(_colour_boundary);
        panel.setPreferredSize(new Dimension(_width_per_unit, _height_per_unit * (project_names.size() + 1)));

        final JTextField field = new JTextField();
        field.setPreferredSize(_dimension);
        field.setHorizontalAlignment(JTextField.CENTER);

        field.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                destroyGui();

                CommandParser.startProject(field.getText());
            }
        });
        panel.add(field, BorderLayout.CENTER);

        for (final String project_name : project_names) {
            final JButton button = new JButton(project_name);

            setButtonColour(button);
            button.setPreferredSize(_dimension);

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    destroyGui();

                    CommandParser.startProject(project_name);
                }
            });

            panel.add(button);
        }

        addRightButtonReturn(panel);
        addPanelToGui(panel);

        prepareGui();
    }

    /**
     * Create the GUI for selecting the date to show.
     */
    private void startDateMenu(ArrayList<Instant> dates) {
        if (_gui != null) {
            destroyGui();
        }

        if (dates == null) {
            dates = ProjectManager.getListDates();
        }

        _gui = new JFrame("Choose date to inquire");
        _gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel panel = new JPanel();
        panel.setBackground(_colour_boundary);
        panel.setPreferredSize(new Dimension(_width_per_unit, _height_per_unit * dates.size()));

        for (final Instant date : dates) {
            final String text_date = date.toString().split("T")[0];
            final JButton button = new JButton(text_date);

            setButtonColour(button);
            button.setPreferredSize(_dimension);

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final String template = "yyyy-MM-dd HH:mm:ss";

                    final String string_date = button.getText() + " 00:00:00";
                    final Instant date = Instant.ofEpochSecond(
                            TimeLogManager.getSecondStartOfDay(
                                    LocalDateTime.parse(string_date, DateTimeFormatter.ofPattern(template))));

                    showProjectList(
                            ProjectManager.getListProjectWithData(date),
                            new ArrayList<>(Arrays.asList(new Instant[]{date})));
                }
            });

            panel.add(button);
        }

        addRightButtonReturn(panel);
        addPanelToGui(panel);

        prepareGui();
    }

    private void mainMenu() {
        if (_gui != null) {
            destroyGui();
        }

        _gui = new JFrame("Project Time Manager");
        _gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JButton button_start = new JButton("START");
        final JButton button_list = new JButton("PROJECTS");
        final JButton button_date = new JButton("DATE");

        setButtonColour(button_start);
        setButtonColour(button_list);
        setButtonColour(button_date);

        button_start.setPreferredSize(_dimension);
        button_list.setPreferredSize(_dimension);
        button_date.setPreferredSize(_dimension);

        button_start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startProjectMenu();
            }
        });

        button_list.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showProjectList();
            }
        });

        button_date.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startDateMenu(null);
            }
        });

        final JPanel panel = new JPanel();

        panel.setBackground(_colour_boundary);
        panel.setPreferredSize(new Dimension(_width_per_unit, _height_per_unit * 2));

        panel.add(button_start);
        panel.add(button_list);
        panel.add(button_date);

        addPanelToGui(panel);

        prepareGui();
    }

    /**
     * Create the main GUI.
     */
    public GuiManager() {
        mainMenu();
    }
}
