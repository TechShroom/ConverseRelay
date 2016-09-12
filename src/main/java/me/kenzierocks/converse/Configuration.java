package me.kenzierocks.converse;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import me.kenzierocks.converse.gsonadapt.ImmutableListDeserializer;
import me.kenzierocks.converse.gsonadapt.OptionalIntAdapter;
import me.kenzierocks.converse.gsonadapt.autovalue.AutoValueAdapterFactory;
import me.kenzierocks.converse.util.FunctionEx;

public class Configuration {

    private static final class SaveThreadProvider implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        private SaveThreadProvider() {
            SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = "pool-" + poolNumber.getAndIncrement() + "-converse-save-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement());
            if (!t.isDaemon())
                t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    private static final int CONFIG_VERSION = 1;

    private static final Gson dataHandler;
    private static final Gson cloneHandler;
    static {
        GsonBuilder builder = new GsonBuilder().serializeNulls().disableHtmlEscaping()
                .registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer())
                .registerTypeAdapterFactory(new AutoValueAdapterFactory())
                .registerTypeAdapter(OptionalInt.class, OptionalIntAdapter.INSTANCE);
        cloneHandler = builder.create();
        dataHandler = builder.setPrettyPrinting().create();
    }

    private static Path getConfigPath() {
        Path configPath = Paths.get(".").resolve("config").resolve("config.cfg");
        try {
            Files.createDirectories(configPath.getParent());
            if (Files.notExists(configPath)) {
                LOGGER.debug("Creating config file at " + configPath.toAbsolutePath());
                Files.createFile(configPath);
                new Configuration().saveConfig();
            }
        } catch (IOException e) {
            LOGGER.warn("Error setting up config", e);
        }
        return configPath;
    }

    public static Configuration loadConfig() throws IOException {
        Configuration config = useFile(getConfigPath(), path -> {
            try (Reader reader = Files.newBufferedReader(path)) {
                LOGGER.debug("Loading config file");
                return dataHandler.fromJson(reader, Configuration.class);
            }
        });
        if (config.getConfigVersion() < CONFIG_VERSION) {
            // Upgrade!
            // ....
            // when it's needed.
            RuntimeException ex = new IllegalStateException(
                    "Cannot convert config version " + config.getConfigVersion() + " to " + CONFIG_VERSION);
            LOGGER.warn("Rewriting config due to error while upgrading.", ex);
            Path oldPath = getConfigPath();
            Path newPath = oldPath.getParent().resolve(oldPath.getFileName() + ".bak");
            LOGGER.warn("Backing up old config to " + newPath.toAbsolutePath());
            try {
                config.saveConfig(newPath);
            } catch (Throwable t) {
                ex.addSuppressed(t);
            }
            throw ex;
        }
        config.saveConfig();
        return config;
    }

    private static final ExecutorService SAVE_THREADS = Executors.newSingleThreadExecutor(new SaveThreadProvider());
    private static final LoadingCache<Path, ReentrantLock> interThreadFileLocks =
            CacheBuilder.newBuilder().weakValues().build(CacheLoader.from(() -> new ReentrantLock()));

    private static ReentrantLock holdFileLock(Path path) {
        ReentrantLock lock = interThreadFileLocks.getUnchecked(path);
        lock.lock();
        return lock;
    }

    private static <T, E extends Exception> T useFile(Path file, FunctionEx<Path, T, E> user) throws E {
        ReentrantLock lock = null;
        try {
            lock = holdFileLock(file);
            return user.apply(file);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    public void saveConfig() throws IOException {
        saveConfig(getConfigPath());
    }

    private void saveConfig(Path path) throws IOException {
        // Syncs up an async save.
        try {
            Optional<IOException> ioError = saveConfigAsync(path).get();
            if (ioError.isPresent()) {
                throw ioError.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            // wut
            throw Throwables.propagate(e);
        }
    }

    public CompletableFuture<Optional<IOException>> saveConfigAsync() {
        return saveConfigAsync(getConfigPath());
    }

    private CompletableFuture<Optional<IOException>> saveConfigAsync(Path path) {
        Configuration crossThreadSafetyNet = cloneByJson();
        return CompletableFuture.supplyAsync(() -> {
            try {
                return useFile(path, p -> {
                    try (Writer writer = Files.newBufferedWriter(path)) {
                        LOGGER.debug("Saving config file to " + path.toAbsolutePath());
                        dataHandler.toJson(crossThreadSafetyNet, writer);
                    } catch (IOException t) {
                        return Optional.of(t);
                    }
                    return Optional.empty();
                });
            } catch (Exception wtfJavac) {
                // This block is due to javac being stupid.
                throw Throwables.propagate(wtfJavac);
            }
        }, SAVE_THREADS);
    }

    private void saveConfigOrLogException() {
        Consumer<Exception> log = e -> LOGGER.warn("error saving config", e);
        try {
            OhNoMoreFutures.whenCompleted(saveConfigAsync(), t -> {
                t.ifPresent(log);
            });
        } catch (Exception ex) {
            log.accept(ex);
        }
    }

    private Configuration cloneByJson() {
        // might be expensive.
        JsonElement tree = cloneHandler.toJsonTree(this);
        return cloneHandler.fromJson(tree, Configuration.class);
    }

    private int configVersion = CONFIG_VERSION;
    private List<Network> networks = new ArrayList<>();
    private Defaults defaults = Defaults.builder().build();
    private WindowSettings windowSettings = WindowSettings.builder().width(800).height(600).build();

    public int getConfigVersion() {
        return this.configVersion;
    }

    public List<Network> getNetworks() {
        return ImmutableList.copyOf(this.networks);
    }

    public void transformNetworks(Function<List<Network>, List<Network>> transform) {
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

    public void transformWindowSettings(Function<WindowSettings, WindowSettings> transform) {
        setWindowSettings(transform.apply(getWindowSettings()));
    }

    public void setWindowSettings(WindowSettings windowSettings) {
        this.windowSettings = windowSettings;
        saveConfigOrLogException();
    }

}
