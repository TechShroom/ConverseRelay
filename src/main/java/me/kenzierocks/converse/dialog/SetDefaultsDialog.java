package me.kenzierocks.converse.dialog;

import static com.google.common.base.Preconditions.checkState;

import java.util.Optional;

import javafx.collections.ObservableList;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import me.kenzierocks.converse.ConverseRelay;
import me.kenzierocks.converse.Defaults;
import me.kenzierocks.converse.dialog.Form.FieldEntry;
import me.kenzierocks.converse.dialog.Form.FormImplementer;
import me.kenzierocks.converse.util.IRCUtil;

public class SetDefaultsDialog extends Dialog<Defaults> implements FormImplementer {

    private static final FieldEntry NICK_NAME = new FieldEntry("nick-name", "Nick name");
    private static final FieldEntry REAL_NAME = new FieldEntry("real-name", "Real name");
    private static final FieldEntry PASSWORD = new FieldEntry("password", "Password");
    private static final FieldEntry ACCOUNT_NAME = new FieldEntry("account-name", "Account name");

    private static final ButtonType CANCEL = ButtonType.CANCEL;
    private static final ButtonType CREATE = new ButtonType("Save", ButtonData.OK_DONE);

    private final Form form;

    public SetDefaultsDialog() {
        ObservableList<ButtonType> buttonTypes = getDialogPane().getButtonTypes();
        buttonTypes.clear();
        buttonTypes.add(CANCEL);
        buttonTypes.add(CREATE);
        getDialogPane().setContent(this.form = Form.implement(this, NICK_NAME, REAL_NAME, PASSWORD, ACCOUNT_NAME));
        setResultConverter(this::convert);
        setTitle("Configure Defaults");
    }

    @Override
    public void setDefaults(Form form) {
        Defaults old = ConverseRelay.CONFIG.getDefaults();
        form.setInputBoxContents(NICK_NAME, old.getNickName());
        form.setInputBoxContents(REAL_NAME, old.getRealName());
        form.setInputBoxContents(PASSWORD, old.getPassword());
        form.setInputBoxContents(ACCOUNT_NAME, old.getAccountName());
    }

    @Override
    public void attachValidation(Form form) {
        form.attachValidator(NICK_NAME, IRCUtil::isValidNickName);
        form.attachValidator(REAL_NAME, IRCUtil::isValidRealName);
    }

    @Override
    public void onValidateStateChange(Form form, boolean valid) {
        Optional.ofNullable(getDialogPane().lookupButton(CREATE)).ifPresent(b -> b.setDisable(!valid));
    }

    protected Defaults convert(ButtonType bt) {
        if (bt == CANCEL) {
            return null;
        }
        checkState(bt == CREATE);
        Defaults.Builder builder = Defaults.builder();
        this.form.applyBoxContents(NICK_NAME, builder::nickName);
        this.form.applyBoxContents(REAL_NAME, builder::realName);
        this.form.applyBoxContents(PASSWORD, builder::password);
        this.form.applyBoxContents(ACCOUNT_NAME, builder::accountName);
        return builder.build();
    }

}
