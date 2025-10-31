package mod.swinegraphics.controller;

import java.io.File;
import java.util.Optional;
import javafx.stage.FileChooser;
import mod.swinegraphics.Context;

/**
 *
 * @author Nani
 */
public enum Dialogs {
    
    PNG("PNG file (*.png)", "*.png"),
    ANIF("ANIF file (*.anif)", "*.anif"),
    DXT("DXT file (*.dxt)", "*.dxt");

    private final String type;
    private final String extension;

    private Dialogs(String type, String extension) {
        this.type = type;
        this.extension = extension;
    }

    public final Optional<File> browseFile(File folder) {
        var fc = initFC(folder, "Browse for file");
        return Optional.ofNullable(fc.showOpenDialog(Context.getWindow()));
    }

    public final Optional<File> saveFile(File folder, String filename) {
        var fc = initFC(folder, "Save file");
        fc.setInitialFileName(filename);
        return Optional.ofNullable(fc.showSaveDialog(Context.getWindow()));
    }

    private FileChooser initFC(File folder, String title) {
        var fc = new FileChooser();
        fc.setTitle(title);
        fc.setInitialDirectory(folder);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(this.type, this.extension));
        return fc;
    }
}
