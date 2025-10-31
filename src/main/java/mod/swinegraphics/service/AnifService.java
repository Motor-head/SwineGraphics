package mod.swinegraphics.service;

import java.io.File;
import java.util.function.Consumer;
import javafx.scene.image.Image;
import mod.swinegraphics.controller.Msg.MsgFormat;

/**
 *
 * @author Nani
 */
public interface AnifService extends Service {

    Image parseAnif(File source);

    MsgFormat export(File destination, Consumer<String> fileReporter);
}
