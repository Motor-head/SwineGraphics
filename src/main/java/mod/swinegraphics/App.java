package mod.swinegraphics;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Dialog;
import mod.swinegraphics.controller.PrimaryController;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        var primary = new FXMLLoader(getClass().getResource("primary.fxml"));
        var dialog = new Dialog<String>();
        dialog.setDialogPane(primary.load());
        dialog.setTitle(Context.getTitle());
        dialog.show();
        PrimaryController con = primary.getController();
        con.init();
        dialog.setOnCloseRequest(con::shutdown);
    }

    public static void main(String[] args) {
        launch();
    }

}
