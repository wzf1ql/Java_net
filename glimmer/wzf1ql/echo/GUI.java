package glimmer.wzf1ql.echo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class GUI
{
    JFrame frame;
    private boolean pushed = false;
    private JTextField jTextField = new JTextField("");
    private JPanel panel = new JPanel();
    JButton okButton = new JButton("确定");
    public GUI(String name)
    {
        frame = new JFrame(name);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        frame.setLayout(null);


        frame.setSize(350, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JScrollPane jScrollPane = new JScrollPane(panel);
        jScrollPane.setBounds(5,5,300,150);
        okButton.setBounds(250,155,60,20);
        jTextField.setBounds(5,155,180,20);

        frame.add(jScrollPane);
        frame.add(okButton);
        frame.add(jTextField);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pushed = true;
                addText(String.valueOf(pushed));
            }
        });

        frame.setVisible(true);
    }
    public String getText()
    {
        if(pushed)
        {
            String s;
            s = jTextField.getText();
            jTextField.setText("");
            jTextField.validate();
            pushed=false;
            return s;
        }
        return "";
    }
    public void addText(String text)
    {
        panel.add(new JLabel(text));
        panel.validate();
    }
    public static void main(String [] args) throws IOException
    {
        GUI gui = new GUI("");
        //gui.addText("asd");
    }
}
