module mod.swinegraphics {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;
    requires java.base;
    requires java.logging;
    requires aircompressor;
    
    opens mod.swinegraphics to javafx.fxml;
    opens mod.swinegraphics.controller to javafx.fxml;
    exports mod.swinegraphics;
}