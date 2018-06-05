import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class GuiWin {

    private enum Mod {
        StartProject,
        ListProject,
        ShowPorject,
        ListProjectLogs,
        ListDates
    }

    final static private int _width_per_unit = 400;
    final static private int _height_per_unit = 100;
    final static private int _size_overhead = 10;

    final static private Color _colour_background = Color.GRAY;
    final static private Color _colour_foreground = Color.WHITE;
    final static private Color _colour_boundary = Color.LIGHT_GRAY;

    private JFrame _gui;

    final static private Dimension _dimension = new Dimension(
            _width_per_unit, _height_per_unit - _size_overhead);

    final static private Dimension _dimension_gui = new Dimension(
            _width_per_unit + _size_overhead * 2, _height_per_unit * 3 - _size_overhead * 2);

    static private void setButtonColour(JButton button) {
        button.setBackground(_colour_background);
        button.setForeground(_colour_foreground);
    }

    private void destroyGui() {
        _gui.setVisible(false);
        _gui.dispose();
        _gui = null;
    }

    private void addPanelToGui(JPanel panel) {
        final JScrollPane scrollable = new JScrollPane(panel);
        scrollable.setPreferredSize(_dimension_gui);
        _gui.add(scrollable);
    }

    private void prepareGui() {
        _gui.pack();
        _gui.setVisible(true);
    }

    private void showProjectList() {
        assert _gui == null : "GUI occupied.";

        _gui = new JFrame("List of Projects");

        _gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel panel = new JPanel();

        final ArrayList<String> project_names = new ProjectManager().getListProject();

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

    private void showProjects(String project_name) {
        assert _gui == null : "GUI occupied.";

        _gui = new JFrame("More on Project " + project_name);
        _gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel panel = new JPanel();

        panel.setBackground(_colour_boundary);
        panel.setPreferredSize(new Dimension(_width_per_unit, _height_per_unit));

        final ProjectManager pm = new ProjectManager(project_name);
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

    private void startProjectMenu() {
        final ArrayList<String> project_names = new ProjectManager().getListProject();

        _gui = new JFrame("Choose Project to Start");
        _gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel panel = new JPanel();
        panel.setBackground(_colour_boundary);
        panel.setPreferredSize(new Dimension(_width_per_unit, _height_per_unit * (project_names.size() + 1)));

        final JTextField field = new JTextField();
        field.setPreferredSize(_dimension);

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
                    _gui.setVisible(false);
                    _gui.dispose();
                    _gui = null;

                    CommandParser.startProject(project_name);
                }
            });

            panel.add(button);
        }

        addPanelToGui(panel);

        prepareGui();
    }

    public GuiWin(Mod mod, String[] params) {
        switch (mod) {
            case ListProject: {
                showProjectList();
                break;
            }
            case ShowPorject: {
                assert params.length == 1;
                showProjects(params[0]);
                break;
            }
            case StartProject: {
                startProjectMenu();
                break;
            }
            default: {
                break;
            }
        }
    }

    public GuiWin() {
        assert _gui == null : "GUI occupied.";

        _gui = new JFrame("Project Time Manager");
        _gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JButton button_start = new JButton("START");
        final JButton button_list = new JButton("LIST");

        setButtonColour(button_start);
        setButtonColour(button_list);

        button_start.setPreferredSize(_dimension);
        button_list.setPreferredSize(_dimension);

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

        final JPanel panel = new JPanel();

        panel.setBackground(_colour_boundary);
        panel.setPreferredSize(new Dimension(_width_per_unit, _height_per_unit * 2));

        panel.add(button_start);
        panel.add(button_list);

        addPanelToGui(panel);

        prepareGui();
    }

}
