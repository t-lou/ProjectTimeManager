import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class GuiWin {
    /**
     * The enum for initialization of GUI.
     */
    private enum Mod {
        StartProject,
        ListProject,
        ShowPorject,
        ListDates
    }

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
    static private void setButtonColour(JButton button) {
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
    private void addPanelToGui(JPanel panel) {
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
        showProjectList(ProjectManager.getListProject());
    }

    /**
     * Show the given projects.
     *
     * @param project_names Names of the given projects.
     */
    private void showProjectList(ArrayList<String> project_names) {
        assert _gui == null : "GUI occupied.";

        _gui = new JFrame("List of Projects");

        _gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel panel = new JPanel();

        panel.setBackground(_colour_boundary);
        panel.setPreferredSize(new Dimension(_width_per_unit, _height_per_unit * project_names.size()));

        for (final String project_name : project_names) {
            final String text = project_name + ": " + CommandParser.getTextForDuration(
                    Duration.ofMillis(new ProjectManager(project_name).getTotalTimeMs()));
            final JButton button = new JButton(text);

            setButtonColour(button);
            button.setPreferredSize(_dimension);

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    destroyGui();

                    new GuiWin(Mod.ShowPorject, new String[]{project_name});
                }
            });

            panel.add(button);
        }

        addPanelToGui(panel);

        prepareGui();
    }

    /**
     * Show the details of one single project.
     *
     * @param project_name The name of The project.
     */
    private void showProject(String project_name) {
        assert _gui == null : "GUI occupied.";

        _gui = new JFrame("More on Project " + project_name);
        _gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel panel = new JPanel();

        panel.setBackground(_colour_boundary);

        final ProjectManager pm = new ProjectManager(project_name);
        // todo check the existance of project.
        final HashMap<Long, ArrayList<Interval>> grouped_log = pm.getGroupedLog();

        ArrayList<Long> keys = new ArrayList<>(grouped_log.keySet());
        Collections.sort(keys);

        String text = "<html>";

        for (final Long day : keys) {
            text += Instant.ofEpochSecond(day * 24L * 3600L).toString().split("T")[0];

            final long duration_ms = grouped_log.get(day).stream().map(Interval::getDurationMs)
                    .mapToLong(l -> l).sum();

            text += ": " + CommandParser.getTextForDuration(Duration.ofMillis(duration_ms)) + "<br/>";

            for (Interval interval : grouped_log.get(day)) {
                text += "  " + interval.formatDateTime() + "<br/>";
            }

            text += "<br/>";
        }

        text += "</html>";

        JLabel label = new JLabel(text);

        panel.add(label);

        addPanelToGui(panel);

        prepareGui();
    }

    /**
     * Show the main manu for starting one project, either available or new.
     */
    private void startProjectMenu() {
        final ArrayList<String> project_names = new ProjectManager().getListProject();

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

        addPanelToGui(panel);

        prepareGui();
    }

    /**
     * Create the GUI for selecting the date to show.
     */
    private void startDateMenu() {
        final ArrayList<Instant> dates = ProjectManager.getListDates();

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
                    destroyGui();

                    final String string_date = button.getText() + " 00:00:00";
                    final Long date = LocalDateTime.parse(string_date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            .toLocalDate()
                            .toEpochDay();

                    showProjectList(ProjectManager.getListProjectWithData(Instant.ofEpochSecond(date * 24L * 3600L)));
                }
            });

            panel.add(button);
        }

        addPanelToGui(panel);

        prepareGui();
    }

    /**
     * The constructor for specific module.
     *
     * @param mod Enum for the module.
     * @param params Further parameters.
     */
    public GuiWin(Mod mod, String[] params) {
        switch (mod) {
            case ListProject: {
                showProjectList();
                break;
            }
            case ShowPorject: {
                assert params.length == 1;
                showProject(params[0]);
                break;
            }
            case StartProject: {
                startProjectMenu();
                break;
            }
            case ListDates: {
                startDateMenu();
                break;
            }
            default: {
                break;
            }
        }
    }

    /**
     * Create the main GUI.
     */
    public GuiWin() {
        assert _gui == null : "GUI occupied.";

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
                destroyGui();

                new GuiWin(Mod.StartProject, new String[0]);
            }
        });

        button_list.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                destroyGui();

                new GuiWin(Mod.ListProject, new String[0]);
            }
        });

        button_date.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                destroyGui();

                new GuiWin(Mod.ListDates, new String[0]);
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

}
