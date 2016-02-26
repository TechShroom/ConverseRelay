package me.kenzierocks.converse.dialog;

import static com.google.common.base.Preconditions.checkState;

import java.util.Optional;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;

import javafx.collections.ObservableList;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import me.kenzierocks.converse.ConverseRelay;
import me.kenzierocks.converse.Network;
import me.kenzierocks.converse.dialog.Form.FieldEntry;
import me.kenzierocks.converse.dialog.Form.FormImplementer;
import me.kenzierocks.converse.util.IRCUtil;

public class AddNetworkDialog extends Dialog<Network>
        implements FormImplementer {

    private static final FieldEntry NET_NAME =
            new FieldEntry("network-name", "Network name");
    private static final FieldEntry NET_ADDR =
            new FieldEntry("network-address", "Network address");
    private static final FieldEntry NET_PORT =
            new FieldEntry("network-port", "Network port");
    private static final FieldEntry NICK_NAME =
            new FieldEntry("nick-name", "Nick name");

    private static final Pattern HOST_ADDRESS_REGEX;
    static {
        // black magic regex
        String regex =
                "^(?=.{1,255}$)[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?(?:\\.[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?)*\\.?$";
        HOST_ADDRESS_REGEX = Pattern.compile(regex);
    }

    private static final ButtonType CANCEL = ButtonType.CANCEL;
    private static final ButtonType CREATE =
            new ButtonType("Create", ButtonData.OK_DONE);

    private final Form form;

    public AddNetworkDialog() {
        ObservableList<ButtonType> buttonTypes =
                getDialogPane().getButtonTypes();
        buttonTypes.clear();
        buttonTypes.add(CANCEL);
        buttonTypes.add(CREATE);
        getDialogPane().setContent(this.form =
                Form.implement(this, NET_NAME, NET_ADDR, NET_PORT, NICK_NAME));
        setResultConverter(this::convert);
        setTitle("Add Network");
    }

    @Override
    public void setDefaults(Form form) {
        form.setInputBoxContents(NET_PORT, Network.DEFAULT_PORT);
        form.setInputBoxContents(NICK_NAME,
                ConverseRelay.CONFIG.getDefaults().getNickName());
    }

    @Override
    public void attachValidation(Form form) {
        form.attachValidator(NET_PORT, val -> {
            try {
                int i = Integer.parseInt(val);
                return 0 < i && i <= 65565;
            } catch (NumberFormatException nope) {
                return false;
            }
        });
        form.attachValidator(NET_ADDR, val -> {
            if (!InetAddresses.isInetAddress(val)) {
                int dot = val.indexOf('.');
                if (dot < 0 || dot == val.length() - 1) {
                    return false;
                }
                // validate as host
                return HOST_ADDRESS_REGEX.matcher(val).matches();
            }
            return true;
        });
        form.attachValidator(NET_NAME, val -> !Strings.isNullOrEmpty(val));
        form.attachValidator(NICK_NAME, IRCUtil::isValidNickName);
    }

    @Override
    public void onValidateStateChange(Form form, boolean valid) {
        Optional.ofNullable(getDialogPane().lookupButton(CREATE))
                .ifPresent(b -> b.setDisable(!valid));
    }

    protected Network convert(ButtonType bt) {
        if (bt == CANCEL) {
            return null;
        }
        checkState(bt == CREATE);
        Network.Builder builder = Network.builder();
        this.form.applyBoxContents(NET_NAME, builder::forcedNetworkName);
        this.form.applyBoxContents(NET_ADDR, builder::networkAddress);
        this.form.applyBoxContents(NET_PORT, s -> {
            try {
                builder.networkPort(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                // Should be impossible....validation should have stopped it.
            }
        });
        this.form.applyBoxContents(NICK_NAME, builder::nickName);
        return builder.build();
    }

}
