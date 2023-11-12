import javax.swing.*;
import java.awt.*;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class GUI {
    private JFrame frame;
    private JTabbedPane tabs;

    private Map<String, Integer> userTabIndex;
    public GUI() {
        userTabIndex = new HashMap<>();
        frame = new JFrame("test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(640, 480));

        Container contentPane = frame.getContentPane();

        tabs = new JTabbedPane();
        tabs.setTabPlacement(SwingConstants.LEFT);
        contentPane.add(tabs, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        contentPane.add(bottomPanel, BorderLayout.PAGE_END);

        JButton newMessage = new JButton("+");
        bottomPanel.add(newMessage, BorderLayout.LINE_START);

        JTextArea area = new JTextArea(5, 40);
        bottomPanel.add(area, BorderLayout.CENTER);

        JButton sendMessage = new JButton("Test");
        bottomPanel.add(sendMessage, BorderLayout.LINE_END);

        frame.pack();
        frame.setVisible(true);
    }

    public void addMessage(Message message) {
        if(!userTabIndex.containsKey(message.username())) {
            tabs.addTab(message.username(), new JPanel());
            userTabIndex.put(message.username(), tabs.getTabCount() - 1);
        }


        JPanel test = (JPanel) tabs.getComponentAt(userTabIndex.get(message.username()));

        JLabel j = new JLabel(message.username() + ": " + message.content() + "\n");
        test.add(j);

        frame.pack();
    }
}
