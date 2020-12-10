package de.dosmike.sponge.pluginpublisher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TextInputPrompt extends JFrame {

    private String result=null;
    private JTextArea textInput;

    public TextInputPrompt(String title, String message) {
        setTitle(title);

        setLayout(new BorderLayout());
        JLabel lblMessage = new JLabel(message);
        lblMessage.setFont(lblMessage.getFont().deriveFont(Font.PLAIN));
        lblMessage.setBorder(new EmptyBorder(4,8,4,8));
        add("North",lblMessage);

        textInput = new JTextArea();
        JScrollPane scroll = new JScrollPane(textInput);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        Dimension prefDims = new Dimension(400,300);
        scroll.setPreferredSize(prefDims);
        scroll.setMinimumSize(prefDims);
        scroll.setSize(prefDims);
        add("Center",scroll);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bnOk = new JButton("OK");
        bnOk.addActionListener(e -> {result=textInput.getText(); dispose();});
        buttons.add(bnOk);
        add("South", buttons);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null); //center
        setVisible(true);
        //bring to front
        setAlwaysOnTop(true);
        setAlwaysOnTop(false);
        //lock packed size as min size
        setMinimumSize(getSize());
    }

    /** @return the text supplied by the user if they pressed OK, null otherwise */
    public String getResult() {
        return result;
    }

    /** sleep the thread repeatedly until the window is no longer visible and disposed */
    public void waitInput() {
        while (isDisplayable()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) { }
        }
    }

}
