package me.kenzierocks.converse;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.kenzierocks.converse.gsonadapt.OptionalIntAdapter;
import me.kenzierocks.converse.gsonadapt.autovalue.AutoValueAdapterFactory;

public class Configuration {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Configuration.class);

    private static final Gson dataHandler = new GsonBuilder()
            .setPrettyPrinting().serializeNulls().disableHtmlEscaping()
            .registerTypeAdapterFactory(new AutoValueAdapterFactory())
            .registerTypeAdapter(OptionalInt.class, OptionalIntAdapter.INSTANCE)
            .create();

    private static Path getConfigPath() {
        Path configPath =
                Paths.get(".").resolve("config").resolve("config.cfg");
        try {
            Files.createDirectories(configPath.getParent());
            if (Files.notExists(configPath)) {
                LOGGER.debug("Creating config file at "
                        + configPath.toAbsolutePath());
                Files.createFile(configPath);
                new Configuration().saveConfig();
            }
        } catch (IOException e) {
            LOGGER.warn("Error setting up config", e);
        }
        return configPath;
    }

    public static Configuration loadConfig() throws IOException {
        Configuration config;
        try (
                Reader reader = Files.newBufferedReader(getConfigPath())) {
            LOGGER.debug("Loading config file");
            config = dataHandler.fromJson(reader, Configuration.class);
        }
        config.saveConfig();
        return config;
    }

    public void saveConfig() throws IOException {
        try (
                Writer writer = Files.newBufferedWriter(getConfigPath())) {
            LOGGER.debug("Saving config file");
            dataHandler.toJson(this, writer);
        }
    }

    private void saveConfigOrLogException() {
        try {
            saveConfig();
        } catch (IOException io) {
            LOGGER.warn("Error saving config as part of a set call", io);
        }
    }

    private List<Network> networks = new ArrayList<>();
    private Defaults defaults = Defaults.builder().build();
    private WindowSettings windowSettings =
            WindowSettings.builder().width(800).height(600).build();

    public List<Network> getNetworks() {
        return ImmutableList.copyOf(this.networks);
    }

    public void transformNetworks(
            Function<List<Network>, List<Network>> transform) {
        setNetworks(transform.apply(getNetworks()));
    }

    public void setNetworks(List<Network> networks) {
        this.networks = networks;
        saveConfigOrLogException();
    }

    public Defaults getDefaults() {
        return this.defaults;
    }

    public void transformDefaults(Function<Defaults, Defaults> transform) {
        setDefaults(transform.apply(getDefaults()));
    }

    public void setDefaults(Defaults defaults) {
        this.defaults = defaults;
        saveConfigOrLogException();
    }

    public WindowSettings getWindowSettings() {
        return this.windowSettings;
    }

    public void transformWindowSettings(
            Function<WindowSettings, WindowSettings> transform) {
        setWindowSettings(transform.apply(getWindowSettings()));
    }

    public void setWindowSettings(WindowSettings windowSettings) {
        this.windowSettings = windowSettings;
        saveConfigOrLogException();
    }

}
