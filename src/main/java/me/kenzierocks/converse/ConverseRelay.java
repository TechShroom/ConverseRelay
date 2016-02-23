package me.kenzierocks.converse;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

public class ConverseRelay extends Application {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ConverseRelay.class);

    public static final Configuration CONFIG;
    static {
        Configuration tmp;
        try {
            tmp = Configuration.loadConfig();
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw Throwables.propagate(e);
            }
            // Probably a bad config. Try overwriting.
            tmp = new Configuration();
            try {
                tmp.saveConfig();
            } catch (IOException e1) {
                throw Throwables.propagate(e1);
            }
        }
        CONFIG = tmp;
    }

    public static void main(String[] args) throws Exception {
        LOGGER.info("Launching " + ConverseRelay.class.getName()
                + " with arguments " + Arrays.toString(args));
        Application.launch(args);
    }

    private final NetworkManager netManager = new NetworkManager();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("/main.fxml"));
        Platform.runLater(() -> {
            @SuppressWarnings("unchecked")
            TreeView<String> netView =
                    (TreeView<String>) parent.lookup("#network-tree");
            TreeItem<String> root = new TreeItem<>();
            root.getChildren()
                    .addAll(CONFIG.getNetworks().stream()
                            .map(this.netManager::loadNetwork)
                            .collect(Collectors.toList()));
            netView.setRoot(root);
            netView.setShowRoot(false);
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                LOGGER.debug("Detected OSX from os.name="
                        + System.getProperty("os.name")
                        + ", configuring menus for OSX");
                // OSX! DO THE TOOLBARS
                MenuBar menuBar = (MenuBar) parent.lookup("#menubar");
                menuBar.setUseSystemMenuBar(true);
                // This behemoth disables the quit menu since OSX provides one.
                menuBar.getMenus().stream()
                        .filter(x -> x.getText().equals("File")).findFirst()
                        .ifPresent(m -> m.getItems().stream()
                                .filter(x -> x.getText().equals("Quit"))
                                .findFirst()
                                .ifPresent(mi -> mi.setVisible(false)));
                // Set theme
                // AquaFx.style();
            }
        });

        WindowSettings windowSettings = CONFIG.getWindowSettings();
        primaryStage.setScene(new Scene(parent, windowSettings.getWidth(),
                windowSettings.getHeight()));
        primaryStage.setTitle("ConverseRelay");
        primaryStage.centerOnScreen();
        primaryStage.show();
        windowSettings.getX().ifPresent(primaryStage::setX);
        windowSettings.getY().ifPresent(primaryStage::setY);

        primaryStage.xProperty().addListener(this::onXChange);
        primaryStage.yProperty().addListener(this::onYChange);
        primaryStage.widthProperty().addListener(this::onWidthChange);
        primaryStage.heightProperty().addListener(this::onHeightChange);
    }

    private void onWidthChange(ObservableValue<? extends Number> obs,
            Number old, Number newVal) {
        CONFIG.transformWindowSettings(ws -> ws.withWidth(newVal.intValue()));
    }

    private void onHeightChange(ObservableValue<? extends Number> obs,
            Number old, Number newVal) {
        CONFIG.transformWindowSettings(ws -> ws.withHeight(newVal.intValue()));
    }

    private void onXChange(ObservableValue<? extends Number> obs, Number old,
            Number newVal) {
        CONFIG.transformWindowSettings(ws -> ws.withX(newVal.intValue()));
    }

    private void onYChange(ObservableValue<? extends Number> obs, Number old,
            Number newVal) {
        CONFIG.transformWindowSettings(ws -> ws.withY(newVal.intValue()));
    }

}
