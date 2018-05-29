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
        ListProject,
        ShowPorject,
        ListProjectLogs,
        ListDates
    }

    final static private int _width_per_unit = 400;
    final static private int _height_per_unit = 150;
    final static private int _height_overhead = 10;

    private JFrame _gui;

    final static private Dimension _dimension = new Dimension(
            _width_per_unit,
            _height_per_unit - _height_overhead);

    private void showProjectList() {
        assert _gui == null : "GUI occupied.";

        _gui = new JFrame("List of Projects");

        _gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel panel = new JPanel();

        final ArrayList<String> project_names = new ProjectManager().getListProject();

        panel.setPreferredSize(new Dimension(_width_per_unit, _height_per_unit * project_names.size()));

        for (final String project_name : project_names) {
            final String text = project_name + ": " + CommandParser.getTextForDuration(
                    Duration.ofMillis(new ProjectManager(project_name).getTotalTimeMs()));
            final JButton button = new JButton(text);

            button.setPreferredSize(_dimension);

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _gui.setVisible(false);
                    _gui.dispose();
                    _gui = null;

                    new GuiWin(Mod.ShowPorject, new String[]{project_name});
                }
            });

            panel.add(button);
        }

        _gui.add(panel);

        _gui.pack();
        _gui.setVisible(true);
    }

    private void showProjects(String project_name) {
        assert _gui == null : "GUI occupied.";

        _gui = new JFrame("More on Project " + project_name);
        _gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel panel = new JPanel();

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

        _gui.add(panel);

        _gui.pack();

        _gui.setVisible(true);
    }

    public GuiWin(Mod mod, String[] params) {
        switch (mod) {
            case ListProject: {
                showProjectList();
            }
            case ShowPorject: {
                assert params.length == 1;
                showProjects(params[0]);
            }
            default: {

            }
        }
    }

    public GuiWin() {
        assert _gui == null : "GUI occupied.";

        _gui = new JFrame("Project Time Manager");
        _gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JButton button_start = new JButton("START");
        final JButton button_list = new JButton("LIST");

        button_start.setPreferredSize(_dimension);
        button_list.setPreferredSize(_dimension);

        button_list.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _gui.setVisible(false);
                _gui.dispose();
                _gui = null;

                new GuiWin(Mod.ListProject, new String[0]);
            }
        });

        final JPanel panel = new JPanel();

        panel.setPreferredSize(new Dimension(_width_per_unit, _height_per_unit * 2));

        panel.add(button_start);
        panel.add(button_list);

        _gui.add(panel);

        _gui.pack();
        _gui.setVisible(true);
    }

}
