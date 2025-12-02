package zwuiix.colria.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataBase {
    private static DataBase instance;

    public static DataBase getInstance() {
        return instance;
    }

    public final Jdbi jdbi;
    private final ExecutorService readers = Executors.newVirtualThreadPerTaskExecutor();
    private final ExecutorService writers = Executors.newSingleThreadExecutor(Thread.ofVirtual().factory());
    private final HikariDataSource ds;

    public DataBase(String filePath) {
        instance = this;

        Path abs = Paths.get(filePath).toAbsolutePath();
        File parent = abs.toFile().getParentFile();
        if (parent != null) parent.mkdirs();

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("sqlite-jdbc is missing from the shaded jar", e);
        }

        HikariConfig hc = new HikariConfig();
        hc.setDriverClassName("org.sqlite.JDBC");
        hc.setJdbcUrl("jdbc:sqlite:" + abs);
        hc.setMaximumPoolSize(1);
        hc.setMinimumIdle(1);
        hc.setPoolName("Colria-SQLite");

        ds = new HikariDataSource(hc);
        this.jdbi = Jdbi.create(ds);
        this.jdbi.installPlugin(new SqlObjectPlugin());
        this.jdbi.useHandle(h -> {
            h.execute("PRAGMA foreign_keys = ON");
            h.execute("PRAGMA journal_mode = WAL");
            h.execute("PRAGMA synchronous = NORMAL");
            h.execute("PRAGMA busy_timeout = 5000");
        });
    }

    public void close() {
        try {
            jdbi.useHandle(h -> {
                h.execute("PRAGMA optimize");
                h.execute("PRAGMA wal_checkpoint(TRUNCATE)");
            });
        } catch (Exception ignored) {}

        shutdown(readers);
        shutdown(writers);

        if (ds != null) {
            try { ds.close(); } catch (Exception ignored) {}
        }
    }

    private void shutdown(ExecutorService ex) {
        ex.shutdown();
        try {
            if (!ex.awaitTermination(5, TimeUnit.SECONDS)) {
                ex.shutdownNow();
            }
        } catch (InterruptedException ie) {
            ex.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public <E, T> CompletableFuture<T> query(Class<E> ext, java.util.function.Function<E, T> fn) {
        return CompletableFuture.supplyAsync(() -> jdbi.withHandle(h -> fn.apply(h.attach(ext))), readers);
    }

    public <E, T> CompletableFuture<T> write(Class<E> ext, java.util.function.Function<E, T> fn) {
        return CompletableFuture.supplyAsync(() -> jdbi.inTransaction(h -> fn.apply(h.attach(ext))), writers);
    }

    public <E> CompletableFuture<Void> write(Class<E> ext, java.util.function.Consumer<E> fn) {
        return CompletableFuture.runAsync(() -> jdbi.useTransaction(h -> fn.accept(h.attach(ext))), writers);
    }
}
