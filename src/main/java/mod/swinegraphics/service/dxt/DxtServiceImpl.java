package mod.swinegraphics.service.dxt;

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
import mod.swinegraphics.service.DxtService;
import mod.swinegraphics.util.Log;

/**
 *
 * @author Nani
 */
public class DxtServiceImpl implements DxtService {

    private final DxtWorker worker;
    private List<BufferedImage> dxtList;

    public DxtServiceImpl(DxtWorker worker) {
        this.worker = worker;
        dxtList = List.of();
    }

    @Override
    public Image parseDxt(File source) {
        var result = worker.parse(source);
        if (result.isEmpty()) {
            dxtList = List.of();
            return Images.FAILED.get();
        }
        dxtList = result.get();
        return SwingFXUtils.toFXImage(dxtList.getFirst(), null);
    }

    @Override
    public Msg.MsgFormat export(File destination, Consumer<String> fileReporter) {
        if (dxtList.isEmpty()) {
            return new Msg.WARN("No data to export");
        }
        var name = destination.getPath();
        try {
            File into = new File(name.replace(".dxt", ""));
            for (var bi : dxtList) {
                ImageIO.write(bi, "PNG", into);
                fileReporter.accept("Exported " + into);
            }
            return new Msg.INFO("Export successful.");
        } catch (IOException ex) {
            fileReporter.accept("Error writing file");
            Log.log(ex, "Unable to write " + name);
            return new Msg.ERR("Unable to write " + name);
        }
    }

}
