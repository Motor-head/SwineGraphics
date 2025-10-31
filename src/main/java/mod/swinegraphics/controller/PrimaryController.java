package mod.swinegraphics.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import mod.swinegraphics.Context;
import mod.swinegraphics.controller.Msg.MsgFormat;
import mod.swinegraphics.images.Images;
import mod.swinegraphics.service.AnifService;
import mod.swinegraphics.service.DxtService;
import mod.swinegraphics.service.ServiceFactory;

/**
 * FXML Controller class
 *
 * @author Nani
 */
public class PrimaryController implements Initializable {

    @FXML
    private ImageView anifPreview;
    @FXML
    private ImageView dxtPreview;
    @FXML
    private DialogPane dialog;
    @FXML
    private Label anifPathTxt;
    @FXML
    private Label dxtPathTxt;
    @FXML
    private ProgressIndicator anifPI;
    @FXML
    private Button anifDownloadBtn;
    @FXML
    private TextArea anifProgressTxt;
    @FXML
    private ProgressIndicator dxtPI;
    @FXML
    private Button dxtDownloadBtn;
    @FXML
    private TextArea dxtProgressTxt;
    @FXML
    private Rectangle anifPIRect;
    @FXML
    private Rectangle dxtPIRect;

    private final AnifService anifService;
    private final DxtService dxtService;
    private final BackgroundProcess background;
    private String anifFileName;
    private String dxtFileName;
    private File lastFolderPath;

    public PrimaryController() {
        this.anifService = ServiceFactory.getAnifService();
        this.dxtService = ServiceFactory.getDxtService();
        this.background = new BackgroundProcess();
        anifFileName = "";
        dxtFileName = "";
        lastFolderPath = Context.getHome();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        anifPathTxt.setText("");
        dxtPathTxt.setText("");
        anifPreview.setImage(Images.DEFAULT.get());
        dxtPreview.setImage(Images.DEFAULT.get());
        anifPI.setVisible(false);
        dxtPI.setVisible(false);
        anifPIRect.setVisible(false);
        dxtPIRect.setVisible(false);
    }

    @FXML
    private void anifDownload() {
        Dialogs.PNG.saveFile(lastFolderPath, anifFileName).ifPresent(destination -> {
            anifPreview.setDisable(true);
            anifDownloadBtn.setDisable(true);
            Consumer<String> fileReporter = filename
                    -> Platform.runLater(() -> anifProgressTxt.appendText(filename));
            background.start(anifService::export, destination, fileReporter,
                    e -> downloadFailure(anifPreview,anifDownloadBtn,e),
                    () -> parseFailure(anifPreview, anifPI)
            );
        }
        );
    }

    @FXML
    private void dxtDownload() {
        Dialogs.PNG.saveFile(lastFolderPath, dxtFileName).ifPresent(destination -> {
            dxtDownloadBtn.setDisable(true);
            dxtPreview.setDisable(true);
            Consumer<String> fileReporter = filename
                    -> Platform.runLater(() -> dxtProgressTxt.appendText(filename));
            background.start(dxtService::export, destination, fileReporter,
                    e -> downloadFailure(dxtPreview,dxtDownloadBtn,e),
                    () -> parseFailure(dxtPreview, dxtPI)
            );
        }
        );
    }

    @FXML
    private void browseAnif() {
        Dialogs.ANIF.browseFile(lastFolderPath).ifPresent(source -> {
            var name = source.getName();
            anifPathTxt.setText(name);
            anifFileName = name.replace(".anif", "");
            lastFolderPath = source.getParentFile();
            showProgress(anifPI, anifDownloadBtn, anifPIRect, true);
            background.start(anifService::parseAnif, source, e -> {
                anifPreview.setImage(e);
                showProgress(anifPI, anifDownloadBtn, anifPIRect, false);
            }, () -> parseFailure(anifPreview, anifPI));
        });
    }

    @FXML
    private void browseDxt() {
        Dialogs.DXT.browseFile(lastFolderPath).ifPresent(source -> {
            var name = source.getName();
            dxtPathTxt.setText(name);
            dxtFileName = name.replace(".dxt", "");
            lastFolderPath = source.getParentFile();
            showProgress(dxtPI, dxtDownloadBtn, dxtPIRect, false);
            background.start(dxtService::parseDxt, source, e -> {
                dxtPreview.setImage(e);
                showProgress(dxtPI, dxtDownloadBtn, dxtPIRect, false);
            }, () -> parseFailure(dxtPreview, dxtPI));
        });
    }

    public void shutdown(DialogEvent evt) {
        background.shutdown();
    }

    public void init() {
        var stage = (Stage) dialog.getScene().getWindow();
        stage.getIcons().add(Context.getLogo());
    }

    private void parseFailure(ImageView imageView, ProgressIndicator indicator) {
        var error = new Msg.ERR("Error parsing input. Check logs for details.");
        Msg.show(error);
        imageView.setImage(Images.FAILED.get());
        indicator.setVisible(false);
        System.out.println("parseFailure");
    }

    private void showProgress(ProgressIndicator indicator, Button button, Rectangle rect, boolean show) {
        indicator.setVisible(show);
        button.setDisable(show);
        rect.setVisible(show);
        System.out.println("showProgress");
    }
    
    private void downloadFailure(ImageView image,Button button,MsgFormat msg){
        image.setDisable(false);
        button.setDisable(false);
        Msg.show(msg);
        System.out.println("downloadFailure");
    }

}
