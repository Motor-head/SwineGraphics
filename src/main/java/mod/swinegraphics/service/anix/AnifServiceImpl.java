package mod.swinegraphics.service.anix;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import mod.swinegraphics.controller.Msg;
import mod.swinegraphics.images.Images;
import mod.swinegraphics.service.AnifService;
import mod.swinegraphics.service.dxt.DxtWorker;
import mod.swinegraphics.util.Log;

/**
 *
 * @author Nani
 */
public class AnifServiceImpl implements AnifService {

    private final DxtWorker dxtHelper;
    private List<BufferedImage> anifList;

    public AnifServiceImpl(DxtWorker dxtHelper) {
        this.dxtHelper = dxtHelper;
        anifList = List.of();
    }

    @Override
    public Image parseAnif(File source) {
        var result = AnixWorker.parse(dxtHelper, source);
        if (result.isEmpty()) {
            anifList = List.of();
            return Images.FAILED.get();
        }
        anifList = result.get();
        return SwingFXUtils.toFXImage(anifList.getFirst(), null);
    }

    @Override
    public Msg.MsgFormat export(File destination, Consumer<String> fileReporter) {
        if (anifList.isEmpty()) {
            return new Msg.WARN("No data to export.");
        }
        try {
            int i = 0;
            String output = destination.getPath().replace(".png", "");
            for (var frame : anifList) {
                var outputFile = new File(output + "-" + i++ + ".png");
                ImageIO.write(frame, "PNG", outputFile);
                fileReporter.accept("Exported " + outputFile + "\n");
            }
            fileReporter.accept("Files exported successfully.");
            return new Msg.INFO("Images successfuly exported to " + destination.getPath());
        } catch (IOException ex) {
            final String errmsg = "Error writing image.";
            fileReporter.accept(errmsg);
            Log.log(ex, errmsg);
            return new Msg.ERR(errmsg);
        }
    }
}
