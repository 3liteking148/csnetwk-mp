import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GUIControllerReal {
    @FXML
    private Text output;

    @FXML
    private TextField input;

    private AtomicReference<String> cmd = new AtomicReference<>("");

    public void initialize() {
        input.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent evt) {
                if (evt.getCode().equals(KeyCode.ENTER)) {
                    // do not write when readLine is reading
                    synchronized(GUIControllerReal.class) {
                        cmd.set(input.getText());
                    }
                    input.setText("");
                }
            }
        });

        output.setFont(Font.loadFont("file:RobotoMono-Regular.ttf", 12));
    }

    // do not call in UI thread
    public String readLine() {
        while(cmd.get().equals("")) {
            // wait
        }

        String ret;
        synchronized(GUIControllerReal.class) {
            ret = cmd.get();
            cmd.set("");
        }

        return ret;
    }

    public void println(String text) {
        Platform.runLater(() -> {
            System.out.println(text);
            output.setText(output.getText() + text + "\n");
        });
    }
}
