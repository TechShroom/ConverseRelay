package me.kenzierocks.converse;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.ClientBuilder;
import org.kitteh.irc.client.library.auth.protocol.NickServ;
import org.kitteh.irc.client.library.auth.protocol.SaslPlain;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import me.kenzierocks.converse.gsonadapt.autovalue.AutoGson;

@AutoValue
@AutoGson
public abstract class Network {

    public static final Builder builder() {
        Defaults defaults = ConverseRelay.CONFIG.getDefaults();
        return builderNoDefaults().networkPort(6667).sslSupported(false)
                .channelsToJoinOnStartup(ImmutableList.of())
                .nickName(defaults.getNickName())
                .realName(defaults.getRealName())
                .password(defaults.getPassword())
                .accountName(defaults.getAccountName());
    }

    public static final Builder builderNoDefaults() {
        return new AutoValue_Network.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder {

        Builder() {
        }

        public abstract Builder forcedNetworkName(String val);

        public abstract Builder networkAddress(String val);

        public abstract Builder networkPort(int val);

        public abstract Builder sslSupported(boolean val);

        public abstract Builder nickName(@Nullable String val);

        @Nullable
        public abstract String getNickName();

        public abstract Builder realName(@Nullable String val);

        @Nullable
        public abstract String getRealName();

        public abstract Builder channelsToJoinOnStartup(Collection<String> val);

        public abstract Builder password(@Nullable String val);

        @Nullable
        public abstract String getPassword();

        public abstract Builder accountName(@Nullable String val);

        @Nullable
        public abstract String getAccountName();

        public abstract Network build();

    }

    Network() {
    }

    @Nullable
    abstract String getForcedNetworkName();

    public abstract String getNetworkAddress();

    public abstract int getNetworkPort();

    public abstract boolean isSslSupported();

    public abstract String getNickName();

    @Nullable
    public abstract String getRealName();

    public abstract ImmutableList<String> getChannelsToJoinOnStartup();

    @Nullable
    public abstract String getPassword();

    @Nullable
    public abstract String getAccountName();

    public String getNetworkName() {
        return Optional.ofNullable(getForcedNetworkName())
                .orElseGet(() -> getNetworkAddress() + ":" + getNetworkPort()
                        + (isSslSupported() ? "+" : ""));
    }

    public Client createClient() {
        ClientBuilder builder = Client.builder().serverHost(getNetworkAddress())
                .serverPort(getNetworkPort()).secure(isSslSupported())
                .name(getNetworkName()).nick(getNickName());
        if (getRealName() != null) {
            builder.realName(getRealName());
        }
        Consumer<Client> joinChannels = client -> {
            getChannelsToJoinOnStartup().forEach(client::addChannel);
        };
        builder.afterBuildConsumer(joinChannels);
        if (getPassword() != null) {
            if (getAccountName() != null) {
                Consumer<Client> nickServAndSasl = client -> {
                    client.getAuthManager().addProtocol(new NickServ(client,
                            getAccountName(), getPassword()));
                    client.getAuthManager().addProtocol(new SaslPlain(client,
                            getAccountName(), getPassword()));
                };
                builder.afterBuildConsumer(
                        nickServAndSasl.andThen(joinChannels));
            } else {
                builder.serverPassword(getPassword());
            }
        }
        return builder.build();
    }

}