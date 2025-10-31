package mod.swinegraphics.service;

import java.io.File;
import java.util.function.Consumer;
import javafx.scene.image.Image;
import mod.swinegraphics.controller.Msg.MsgFormat;

/**
 *
 * @author Nani
 */
public interface DxtService extends Service {

    Image parseDxt(File source);

    MsgFormat export(File destination, Consumer<String> fileReporter);
}
