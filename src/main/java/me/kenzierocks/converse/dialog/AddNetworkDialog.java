package me.kenzierocks.converse.dialog;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.SetChangeListener.Change;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import me.kenzierocks.converse.Network;

public class AddNetworkDialog extends Dialog<Network> {

    private enum FieldEntry {
        NET_NAME("network-name"), NET_ADDR("network-address"),
        NET_PORT("network-port");

        private static final int COUNT = values().length;

        private final String baseId;

        FieldEntry(String id) {
            this.baseId = id;
        }

        @Override
        public String toString() {
            return name() + "[baseId=" + this.baseId + "]";
        }

    }

    private enum TackOn {

        INPUT("input"), VALIDATE_MARK("validate-mark");

        private final String tackOn;

        TackOn(String tackOn) {
            this.tackOn = tackOn;
        }

        public String to(FieldEntry entry) {
            return this.tackOn + "-" + entry.baseId;
        }

    }

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AddNetworkDialog.class);
    private static final Pattern HOST_ADDRESS_REGEX;
    static {
        // black magic regex
        String regex =
                "^(?=.{1,255}$)[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?(?:\\.[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?)*\\.?$";
        HOST_ADDRESS_REGEX = Pattern.compile(regex);
    }
    private final ButtonType cancel;
    private final ButtonType create;
    private final ObservableSet<FieldEntry> valid =
            FXCollections.observableSet(EnumSet.noneOf(FieldEntry.class));

    public AddNetworkDialog() {
        this.cancel = ButtonType.CANCEL;
        this.create = new ButtonType("Create", ButtonData.OK_DONE);
        ObservableList<ButtonType> buttonTypes =
                getDialogPane().getButtonTypes();
        buttonTypes.clear();
        buttonTypes.add(this.cancel);
        buttonTypes.add(this.create);

        Parent parent;
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/addnetwork.fxml"));
            loader.setController(this);
            parent = loader.load();
        } catch (IOException e) {
            LOGGER.warn("error loading addNetwork dialog", e);
            return;
        }

        getDialogPane().setContent(parent);
        getDialogPane().setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        getDialogPane().layout();
        setTitle("Add Network");
        setResultConverter(this::buttonTypeToNetwork);
        attachValidation();
        updateCreateButton();
    }

    private void attachValidation() {
        this.valid.addListener(this::onValidatedFieldChange);
        attachValidator(FieldEntry.NET_PORT, val -> {
            try {
                int i = Integer.parseInt(val);
                return 0 < i && i <= 65565;
            } catch (NumberFormatException nope) {
                this.valid.remove(FieldEntry.NET_PORT);
                return false;
            }
        });
        attachValidator(FieldEntry.NET_ADDR, val -> {
            if (!InetAddresses.isInetAddress(val)) {
                if (val.indexOf('.') < 0) {
                    return false;
                }
                // validate as host
                return HOST_ADDRESS_REGEX.matcher(val).matches();
            }
            return true;
        });
        attachValidator(FieldEntry.NET_NAME,
                val -> !Strings.isNullOrEmpty(val.trim()));
    }

    private void onValidatedFieldChange(Change<? extends FieldEntry> change) {
        updateCreateButton();
        if (change.wasAdded()) {
            setValidationMark(change.getElementAdded(), true);
        } else if (change.wasRemoved()) {
            setValidationMark(change.getElementRemoved(), false);
        }
    }

    private void attachValidator(FieldEntry field,
            Predicate<String> validator) {
        Consumer<String> listener = val -> {
            try {
                if (validator.test(val)) {
                    this.valid.add(field);
                } else {
                    this.valid.remove(field);
                }
            } catch (RuntimeException | Error t) {
                // User knows best.
                this.valid.add(field);
                LOGGER.warn(
                        "assuming user knows best for field " + field
                                + ", input: "
                                + Arrays.toString(
                                        val.getBytes(StandardCharsets.UTF_8)),
                        t);
                throw t;
            }
        };
        TextInputControl inputBox = getInputBox(field);
        inputBox.textProperty().addListener((obs, oldVal, newVal) -> {
            listener.accept(newVal);
        });
        listener.accept(inputBox.getText());
    }

    private void updateCreateButton() {
        getDialogPane().lookupButton(this.create)
                .setDisable(this.valid.size() != FieldEntry.COUNT);
    }

    private Network buttonTypeToNetwork(ButtonType bt) {
        if (bt == this.cancel) {
            return null;
        }
        checkState(bt == this.create);
        Network.Builder builder = Network.builder();
        applyBoxContents(FieldEntry.NET_NAME, builder::forcedNetworkName);
        applyBoxContents(FieldEntry.NET_ADDR, builder::networkAddress);
        applyBoxContents(FieldEntry.NET_PORT, s -> {
            try {
                builder.networkPort(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                // Should be impossible....validation should have stopped it.
            }
        });
        return builder.build();
    }

    private void applyBoxContents(FieldEntry field, Consumer<String> apply) {
        String data = getInputBoxContents(field).trim();
        if (!Strings.isNullOrEmpty(data)) {
            apply.accept(data);
        }
    }

    private String getInputBoxContents(FieldEntry id) {
        return getInputBox(id).getText();
    }

    private TextInputControl getInputBox(FieldEntry id) {
        return (TextInputControl) getDialogPane()
                .lookup("#" + TackOn.INPUT.to(id));
    }

    private void setValidationMark(FieldEntry id, boolean on) {
        String checkedBox = "\u2611";
        String uncheckedBox = "\u2610";
        getValidationMarkBox(id).setText(on ? checkedBox : uncheckedBox);
    }

    private Labeled getValidationMarkBox(FieldEntry id) {
        return (Labeled) getDialogPane()
                .lookup("#" + TackOn.VALIDATE_MARK.to(id));
    }

}
