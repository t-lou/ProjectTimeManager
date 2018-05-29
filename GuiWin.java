import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GuiWin extends JFrame {

    final static private int _width_per_unit = 400;
    final static private int _height_per_unit = 100;
    final static private int _height_overhead = 70;

    final static private Dimension _dimension = new Dimension(_width_per_unit, _height_per_unit);

    public GuiWin() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1 * _width_per_unit, 2 * _height_per_unit + _height_overhead);

        final JButton button_start = new JButton("START");
        final JButton button_list = new JButton("LIST");

        button_start.setPreferredSize(_dimension);
        button_list.setPreferredSize(_dimension);

        button_list.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                CommandParser.listProjects();
            }
        });

        final JPanel panel = new JPanel();

        panel.add(button_start);
        panel.add(button_list);

        getContentPane().add(panel);

        setVisible(true);
    }

}
