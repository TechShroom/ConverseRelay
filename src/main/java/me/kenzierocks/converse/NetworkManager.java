package me.kenzierocks.converse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.client.ClientConnectedEvent;
import org.kitteh.irc.lib.net.engio.mbassy.listener.Handler;

import com.google.common.collect.ImmutableMap;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import me.kenzierocks.converse.jfx.StylableTreeItem;
import me.kenzierocks.converse.listeners.AddChannelListener;

/**
 * Handles attaching networks to the tree and connecting the clients.
 */
public class NetworkManager {

    private static final class PerNetworkManager {

        private final StylableTreeItem<String> networkItem;
        private final Network network;
        private final Client client;
        private final Map<String, TreeItem<String>> channelItems;

        private PerNetworkManager(TreeItem<String> networkItem, Network network, Client client,
                Map<String, TreeItem<String>> channelItems) {
            this.networkItem = StylableTreeItem.fromTreeItem(networkItem);
            this.networkItem.getStylableItem().getStyleClass().add("dscn-network");
            this.network = network;
            this.client = client;
            this.channelItems = channelItems;
        }

        @Handler
        public void onConnected(ClientConnectedEvent event) {
            Platform.runLater(() -> {
                this.networkItem.getStylableItem().getStyleClass().remove("dscn-network");
                this.networkItem.getStylableItem().getStyleClass().add("conn-network");
                this.networkItem.getStylableItem().applyCss();
            });
        }

    }

    private final Map<Network, Client> clients = new HashMap<>();

    public TreeItem<String> loadNetwork(Network network) {
        Client client = network.createClient();
        this.clients.put(network, client);
        TreeItem<String> networkNode = new TreeItem<String>(network.getNetworkName());
        String[] channels = network.getChannelsToJoinOnStartup().stream().toArray(String[]::new);
        if (channels.length > 0) {
            client.getEventManager().registerEventListener(new AddChannelListener(channels));
        }
        ImmutableMap.Builder<String, TreeItem<String>> channelItems = ImmutableMap.builder();
        network.getChannelsToJoinOnStartup().stream().forEach(chan -> {
            TreeItem<String> chanNode = new TreeItem<>(chan);
            channelItems.put(chan, chanNode);
            networkNode.getChildren().add(chanNode);
        });
        client.getEventManager()
                .registerEventListener(new PerNetworkManager(networkNode, network, client, channelItems.build()));
        return networkNode;
    }

    public void shutdown() {
        this.clients.forEach((net, conn) -> {
            String quit = Optional.ofNullable(net.getQuitMessage())
                    .orElse(ConverseRelay.CONFIG.getDefaults().getQuitMessage());
            conn.shutdown(quit == null ? "ConverseRelay™" : quit);
        });
    }

}
