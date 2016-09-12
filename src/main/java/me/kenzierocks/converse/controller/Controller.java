package me.kenzierocks.converse.controller;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import me.kenzierocks.converse.CommonRoutes;
import me.kenzierocks.converse.ConverseRelay;
import me.kenzierocks.converse.dialog.AddNetworkDialog;
import me.kenzierocks.converse.dialog.SetDefaultsDialog;

public class Controller {

    @FXML
    public void quit() {
        CommonRoutes.quit();
    }

    @FXML
    public void openAddNetworkDialog() {
        new AddNetworkDialog().showAndWait().ifPresent(net -> {
            ConverseRelay.CONFIG.transformNetworks(netlist -> {
                return Stream.concat(Stream.of(net), netlist.stream()).collect(Collectors.toList());
            });
        });
    }

    @FXML
    public void openSetDefaultsDialog() {
        new SetDefaultsDialog().showAndWait().ifPresent(ConverseRelay.CONFIG::setDefaults);
    }

}
