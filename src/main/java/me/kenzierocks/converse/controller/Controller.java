package me.kenzierocks.converse.controller;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.fxml.FXML;
import me.kenzierocks.converse.ConverseRelay;
import me.kenzierocks.converse.dialog.AddNetworkDialog;

public class Controller {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Controller.class);

    @FXML
    public void quit() {
        Platform.exit();
    }

    @FXML
    public void openAddNetworkDialog() {
        new AddNetworkDialog().showAndWait().ifPresent(net -> {
            ConverseRelay.CONFIG.transformNetworks(netlist -> {
                return Stream.concat(Stream.of(net), netlist.stream())
                        .collect(Collectors.toList());
            });
        });
    }

}
