package mod.swinegraphics;

import java.io.File;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Window;

/**
 *
 * @author Nani
 */
public class Context {

    private static class Holder {

        private static final Context INSTANCE = new Context();
    }

    private final File home;
    private final File logfile;
    private final String title;
    private final Image logo;

    private Window window;

    private Context() {
        title = "SWINE Graphics";
        home = new File(System.getProperty("user.home"));
        logfile = new File(home, "SWINEGraphics_Error.log");
        logo = new Image(getClass().getResource("logo.png").toString());
    }

    public static String getTitle() {
        return Holder.INSTANCE.title;
    }

    public static File getLogFile() {
        return Holder.INSTANCE.logfile;
    }

    public static File getHome() {
        return Holder.INSTANCE.home;
    }

    public static Window getWindow() {
        return Holder.INSTANCE.window;
    }

    public static Image getLogo() {
        return Holder.INSTANCE.logo;
    }

    public static void setWindow(Dialog<String> w) {
        Holder.INSTANCE.window = w.getDialogPane().getScene().getWindow();
    }
}
