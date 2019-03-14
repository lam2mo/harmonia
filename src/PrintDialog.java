package harmonia;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.event.*;

public class PrintDialog extends JDialog implements ActionListener {

    public static void showPlainTextDialog(Frame owner, String caption, String text) {
        showPlainTextDialog(owner, caption, text, false);
    }

    public static void showPlainTextDialog(Frame owner, String caption, String text, boolean fixedWidth) {
        PrintDialog dlg = new PrintDialog(owner, caption);
        dlg.textArea = new JTextArea(text, 30,40);
        dlg.textArea.setLineWrap(true);
        dlg.textArea.setWrapStyleWord(true);
        if (fixedWidth) {
            dlg.textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        }
        dlg.contentPanel.setLayout(new BorderLayout());
        dlg.contentPanel.add(new JScrollPane(dlg.textArea), BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    public static void showRichTextDialog(Frame owner, String caption, String text) {
        PrintDialog dlg = new PrintDialog(owner, caption);
        dlg.richTextPane = new JEditorPane();
        dlg.richTextPane.setContentType("text/rtf");
        dlg.richTextPane.setText(text);
        dlg.contentPanel.setLayout(new BorderLayout());
        dlg.contentPanel.add(new JScrollPane(dlg.richTextPane), BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    public JPanel contentPanel;
    public JTextArea textArea;
    public JEditorPane richTextPane;

    private JButton printButton;
    private JButton exitButton;

    public PrintDialog(Frame owner, String caption) {
        super(owner, caption, true);
        contentPanel = new JPanel();
        textArea = null;
        buildGUI();
        pack();
        setSize(600,800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void buildGUI() {
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));

        printButton = new JButton("Print");
        printButton.addActionListener(this);
        exitButton = new JButton("Close");
        exitButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(printButton);
        buttonPanel.add(exitButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == printButton) {
            try {
                if (textArea != null) {
                    textArea.print();
                }
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(), "Printer Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (evt.getSource() == exitButton) {
            dispose();
        }
    }

}

