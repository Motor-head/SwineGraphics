package mod.swinegraphics.images;

import javafx.scene.image.Image;

/**
 *
 * @author Nani
 */
public enum Images {
    DEFAULT, FAILED;

    public Image get() {
        return new Image(getClass().getResource(this.toString().toLowerCase() + ".png").toString());
    }
}
