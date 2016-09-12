package me.kenzierocks.converse;

import java.util.OptionalInt;

import com.google.auto.value.AutoValue;

import me.kenzierocks.converse.gsonadapt.autovalue.AutoGson;

@AutoValue
@AutoGson
public abstract class WindowSettings {

    public static Builder builder() {
        return new AutoValue_WindowSettings.Builder().x(OptionalInt.empty()).y(OptionalInt.empty());
    }

    @AutoValue.Builder
    public static abstract class Builder {

        public abstract Builder width(int width);

        public abstract Builder height(int height);

        public abstract Builder x(OptionalInt x);

        public Builder x(int x) {
            return x(OptionalInt.of(x));
        }

        public abstract Builder y(OptionalInt y);

        public Builder y(int y) {
            return y(OptionalInt.of(y));
        }

        public abstract WindowSettings build();

    }

    WindowSettings() {
    }

    public abstract int getWidth();

    public final WindowSettings withWidth(int width) {
        return toBuilder().width(width).build();
    }

    public abstract int getHeight();

    public final WindowSettings withHeight(int height) {
        return toBuilder().height(height).build();
    }

    public abstract OptionalInt getX();

    public final WindowSettings withX(int x) {
        return toBuilder().x(x).build();
    }

    public abstract OptionalInt getY();

    public final WindowSettings withY(int y) {
        return toBuilder().y(y).build();
    }

    public abstract Builder toBuilder();

}
