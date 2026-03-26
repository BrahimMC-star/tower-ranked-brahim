package zwuiix.colria.util;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.List;
import java.util.stream.Stream;

public class Fs {
    public static boolean deleteDirectory(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                @NonNull
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    clearReadonlyIfWindows(file);
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                @NonNull
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc != null) throw exc;
                    clearReadonlyIfWindows(dir);
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                @NonNull
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    clearReadonlyIfWindows(file);
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                private void clearReadonlyIfWindows(Path p) {
                    try {
                        String os = System.getProperty("os.name", "").toLowerCase();
                        if (os.contains("win")) {
                            DosFileAttributeView view = Files.getFileAttributeView(
                                    p, DosFileAttributeView.class, LinkOption.NOFOLLOW_LINKS
                            );
                            if (view != null && view.readAttributes().isReadOnly()) {
                                view.setReadOnly(false);
                            }
                        }
                    } catch (IOException ignored) {}
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void copyDirectory(Path source, Path target, CopyOption... options) throws IOException {
        if (!Files.exists(source) || !Files.isDirectory(source))
            throw new NoSuchFileException("Invalid source: " + source);

        try {
            if (Files.exists(target) && Files.isSameFile(source, target))
                throw new IOException("Source and target are the same");
        } catch (NoSuchFileException ignored) {}

        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            @NonNull
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path dest = target.resolve(source.relativize(dir).toString());
                Files.createDirectories(dest);
                return FileVisitResult.CONTINUE;
            }

            @Override
            @NonNull
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path dest = target.resolve(source.relativize(file).toString());
                Files.createDirectories(dest.getParent());
                Files.copy(file, dest, options);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static List<String> getFolders(Path src) {
        if (!Files.isDirectory(src)) return List.of();

        try (Stream<Path> s = Files.list(src)) {
            return s.filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
