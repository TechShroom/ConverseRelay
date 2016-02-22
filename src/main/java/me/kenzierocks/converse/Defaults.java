package me.kenzierocks.converse;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

import me.kenzierocks.converse.gsonadapt.autovalue.AutoGson;

@AutoValue
@AutoGson
public abstract class Defaults {

    public static final Builder builder() {
        return new AutoValue_Defaults.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder {

        Builder() {
        }

        public abstract Builder nickName(String val);

        public abstract Builder realName(String val);

        public abstract Builder password(String val);

        public abstract Builder accountName(String val);

        public abstract Defaults build();

    }

    Defaults() {
    }

    @Nullable
    public abstract String getNickName();

    @Nullable
    public abstract String getRealName();

    @Nullable
    public abstract String getPassword();

    @Nullable
    public abstract String getAccountName();

}
