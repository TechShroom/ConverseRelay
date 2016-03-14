package me.kenzierocks.converse.listeners;

import org.kitteh.irc.client.library.event.client.ClientConnectedEvent;
import org.kitteh.irc.lib.net.engio.mbassy.listener.Handler;

public class AddChannelListener {

    private final String[] channelsToJoin;

    public AddChannelListener(String... channels) {
        this.channelsToJoin = channels.clone();
    }

    @Handler
    public void onConnection(ClientConnectedEvent event) {
        event.getClient().addChannel(this.channelsToJoin);
    }

}
