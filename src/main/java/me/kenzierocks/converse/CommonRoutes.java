package me.kenzierocks.converse;

import javafx.application.Platform;

/**
 * Handles things that are done the same way regardless of source.
 */
public final class CommonRoutes {

    public static void quit() {
        Platform.exit();
    }

    private CommonRoutes() {
        throw new AssertionError();
    }

}
