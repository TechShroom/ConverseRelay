package me.kenzierocks.converse.dialog;

import static com.google.common.base.Preconditions.checkArgument;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener.Change;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

public abstract class Form extends Pane {

    private static final Logger LOGGER = LoggerFactory.getLogger(Form.class);

    public static class FieldEntry {

        protected final String baseId;
        protected final String tag;

        public FieldEntry(String id, String tag) {
            this.baseId = id;
            this.tag = tag;
        }

        @Override
        public String toString() {
            return "FieldEntry[" + this.baseId + "]";
        }

    }

    public static class TackOn {

        protected static final TackOn INPUT = new TackOn("input");
        protected static final TackOn VALIDATE_MARK = new TackOn("validate-mark");

        protected final String tackOn;

        public TackOn(String tackOn) {
            this.tackOn = tackOn;
        }

        public String to(FieldEntry entry) {
            return this.tackOn + "-" + entry.baseId;
        }

    }

    public interface FormImplementer {

        void setDefaults(Form form);

        void attachValidation(Form form);

        void onValidateStateChange(Form form, boolean valid);

    }

    protected static final ButtonType CREATE = new ButtonType("Create", ButtonData.OK_DONE);
    protected static final ButtonType CANCEL = ButtonType.CANCEL;
    private static final String CHECKED_BOX = "\u2611";
    private static final String UNCHECKED_BOX = "\u2610";

    public static Form implement(FormImplementer impl, FieldEntry... entries) {
        class FormDelegate extends Form {

            FormDelegate(FieldEntry[] entries) {
                super(entries);
            }

            @Override
            protected void setDefaults() {
                impl.setDefaults(this);
            }

            @Override
            protected void attachValidation() {
                impl.attachValidation(this);
            }

            @Override
            protected void onValidateStateChange(boolean valid) {
                impl.onValidateStateChange(this, valid);
            }

        }
        return new FormDelegate(entries);
    }

    private static Parent generateParentForEntries(Pane form, List<FieldEntry> entries) {
        Pane parent = form;
        GridPane grid = new GridPane();
        parent.getChildren().add(grid);
        grid.setAlignment(Pos.CENTER);

        grid.setHgap(5);
        grid.setVgap(5);

        ColumnConstraints tmp = new ColumnConstraints();
        tmp.setHgrow(Priority.ALWAYS);
        tmp.setPercentWidth(25);
        grid.getColumnConstraints().add(tmp);
        tmp = new ColumnConstraints();
        tmp.setHalignment(HPos.CENTER);
        tmp.setPercentWidth(35);
        grid.getColumnConstraints().add(tmp);
        tmp = new ColumnConstraints();
        tmp.setHalignment(HPos.CENTER);
        tmp.setPercentWidth(15);
        grid.getColumnConstraints().add(tmp);

        ObservableList<Node> gridChildren = grid.getChildren();
        for (int i = 0; i < entries.size(); i++) {
            generateNodes(i, entries.get(i)).forEach(gridChildren::add);
        }

        return parent;
    }

    private static final Insets MARGIN_5_PX = new Insets(5);

    private static Stream<Node> generateNodes(int row, FieldEntry entry) {
        Label label = new Label(entry.tag + ":");
        GridPane.setColumnIndex(label, 0);
        GridPane.setHalignment(label, HPos.RIGHT);

        TextField input = new TextField();
        input.setId(TackOn.INPUT.to(entry));
        GridPane.setColumnIndex(input, 1);

        Label validate = new Label(UNCHECKED_BOX);
        validate.setId(TackOn.VALIDATE_MARK.to(entry));
        Font font = validate.getFont();
        validate.setFont(Font.font(font.getFamily(), 24));
        GridPane.setColumnIndex(validate, 2);

        return Stream.of(label, input, validate).map(n -> {
            GridPane.setRowIndex(n, row);
            GridPane.setMargin(n, MARGIN_5_PX);
            return n;
        });
    }

    protected final List<FieldEntry> entries;
    protected final ObservableSet<FieldEntry> valid = FXCollections.observableSet();

    protected Form(FieldEntry... entries) {
        checkArgument(entries.length == Stream.of(entries).distinct().count(), "duplicates");
        this.entries = ImmutableList.copyOf(entries);
        generateParentForEntries(this, this.entries);
        setDefaults();
        this.valid.addListener(this::onValidatedFieldChange);
        attachValidation();
    }

    protected abstract void setDefaults();

    protected abstract void attachValidation();

    protected abstract void onValidateStateChange(boolean valid);

    private void onValidatedFieldChange(Change<? extends FieldEntry> change) {
        onValidateStateChange(this.valid.containsAll(this.entries));
        if (change.wasAdded()) {
            setValidationMark(change.getElementAdded(), true);
        } else if (change.wasRemoved()) {
            setValidationMark(change.getElementRemoved(), false);
        }
    }

    /**
     * The validator will never be passed null, and the string will be trimmed.
     */
    public void attachValidator(FieldEntry field, Predicate<String> validator) {
        Consumer<String> listener = val -> {
            try {
                if (val != null && validator.test(val.trim())) {
                    this.valid.add(field);
                } else {
                    this.valid.remove(field);
                }
            } catch (RuntimeException | Error t) {
                // User knows best.
                this.valid.add(field);
                LOGGER.warn("assuming user knows best for field " + field + ", input: "
                        + Arrays.toString(val.getBytes(StandardCharsets.UTF_8)), t);
                throw t;
            }
        };
        TextInputControl inputBox = getInputBox(field);
        inputBox.textProperty().addListener((obs, oldVal, newVal) -> {
            listener.accept(newVal);
        });
        listener.accept(inputBox.getText());
    }

    public void applyBoxContents(FieldEntry field, Consumer<String> apply) {
        String data = getInputBoxContents(field).trim();
        if (!Strings.isNullOrEmpty(data)) {
            apply.accept(data);
        }
    }

    public void setInputBoxContents(FieldEntry id, Object text) {
        getInputBox(id).setText(Optional.ofNullable(text).map(String::valueOf).map(String::trim).orElse(""));
    }

    protected String getInputBoxContents(FieldEntry id) {
        return getInputBox(id).getText();
    }

    protected TextInputControl getInputBox(FieldEntry id) {
        return (TextInputControl) lookup("#" + TackOn.INPUT.to(id));
    }

    protected void setValidationMark(FieldEntry id, boolean on) {
        getValidationMarkBox(id).setText(on ? CHECKED_BOX : UNCHECKED_BOX);
    }

    protected Labeled getValidationMarkBox(FieldEntry id) {
        return (Labeled) lookup("#" + TackOn.VALIDATE_MARK.to(id));
    }

}
