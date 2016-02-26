package me.kenzierocks.converse.jfx;

import static com.google.common.base.Preconditions.checkNotNull;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;

public class StylableTreeItem<T> {
    
    public static StylableTreeItem<String> fromTreeItem(TreeItem<String> item) {
        StylableTreeItem<String> wrapper = new StylableTreeItem<>(item, item.getValue());
        item.setValue("");
        return wrapper;
    }

    private TreeItem<T> item;
    private Node stylableItem;
    private String actualText;

    public StylableTreeItem(TreeItem<T> item, String actualText) {
        this.item = item;
        setActualText(actualText);
    }

    public TreeItem<T> getItem() {
        return this.item;
    }

    public void setActualText(String actualText) {
        this.actualText = checkNotNull(actualText);
        updateText();
    }

    public String getActualText() {
        return this.actualText;
    }

    public Node getStylableItem() {
        return this.stylableItem;
    }

    private void updateText() {
        if (this.item.getGraphic() == null) {
            this.item.setGraphic(this.stylableItem = new Label());
        }
        if (!(this.item.getGraphic() instanceof Label)) {
            return;
        }
        ((Label) this.item.getGraphic()).setText(this.actualText);
    }

}
