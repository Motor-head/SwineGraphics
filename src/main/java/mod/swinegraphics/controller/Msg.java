package mod.swinegraphics.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import mod.swinegraphics.Context;

/**
 *
 * @author Nani
 */
public class Msg {

    public sealed interface MsgFormat permits ERR, INFO, WARN {

        String message();

        Alert.AlertType getType();
    }

    public final record ERR(String message) implements MsgFormat {

        @Override
        public Alert.AlertType getType() {
            return Alert.AlertType.ERROR;
        }
    }

    public final record INFO(String message) implements MsgFormat {

        @Override
        public Alert.AlertType getType() {
            return Alert.AlertType.INFORMATION;
        }
    }

    public final record WARN(String message) implements MsgFormat {

        @Override
        public Alert.AlertType getType() {
            return Alert.AlertType.WARNING;
        }

    }

    static void show(MsgFormat message) {
        if (message == null || message.message().isBlank()) {
            return;
        }
        var popup = message.getType();
        var alert = new Alert(popup, message.message(), ButtonType.OK);
        var stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(Context.getLogo());
        alert.show();
    }
}
