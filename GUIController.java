import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class GUIController {
    @FXML
    private TabPane messagesTab;

    @FXML
    private Button sendButton;

    @FXML
    private TextArea textInput;

    @FXML
    private Button newChatButton;

    // todo: move out of here
    private BlockingQueue<byte[]> messageQueue;
    private String currentUser;

    private Thread javaFxThread;

    public GUIController(BlockingQueue<byte[]> messageQueue, String currentUser) {
        this.messageQueue = messageQueue;
        this.currentUser = currentUser;
    }
    public void initialize() {
        System.out.println("Initializing GUI");

        sendButton.setOnAction(actionEvent -> {
            int curIdx = messagesTab.getSelectionModel().getSelectedIndex();
            Tab currentTab = messagesTab.getTabs().get(curIdx);

            String roomname = ((ExtraInfo)currentTab.getUserData()).roomname;
            Message message = new Message(roomname, currentUser, textInput.getText());
            try {
                messageQueue.add(message.toByteArray());
                addMessage(message);

                // blank
                textInput.setText("");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        newChatButton.setOnAction(actionEvent -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Who");

            Optional<String> username = dialog.showAndWait();
            if(username.isPresent()) {
                addTab(username.get());
            }
        });

        javaFxThread = Thread.currentThread();
    }

    public Thread getJavaFxThread() {
        return javaFxThread;
    }
    private class ExtraInfo {
        public String roomname;

        public ExtraInfo(String roomname) {
            this.roomname = roomname;
        }
    }

    private Tab addTab(String username) {
        ObservableList<Tab> tabs = messagesTab.getTabs();
        int tabCount = tabs.size();

        String tabTitle = username.equals("") ? "Global Chat" : username;

        Tab newTab = new Tab(tabTitle);
        newTab.setContent(new Text());
        newTab.setUserData(new ExtraInfo(username)); // tab title != username if global chat

        messagesTab.getTabs().add(tabCount /* idx */, newTab);


        return newTab;
    }

    private Tab getUserTab(String roomname) {
        // find the tab with the username
        Optional<Tab> userTab = messagesTab.getTabs()
                .stream()
                .filter(t -> ((ExtraInfo)t.getUserData()).roomname.equals(roomname))
                .findFirst();

        if(userTab.isEmpty()) {
            userTab = Optional.of(addTab(roomname));
        }

        return userTab.get();
    }
    public void addMessage(Message message) {
        Tab userTab = getUserTab(message.roomname());
        Text text = (Text) userTab.getContent();
        text.setText(text.getText() + "\n" + message.username() + ": " + message.content());
    }

}
