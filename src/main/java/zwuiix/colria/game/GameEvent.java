package zwuiix.colria.game;

import cn.nukkit.event.Event;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class GameEvent {
    private static final ConcurrentMap<Game, ConcurrentMap<Class<?>,
            CopyOnWriteArrayList<Consumer<?>>>> EVENTS = new ConcurrentHashMap<>();

    public static <T extends Event> Subscription subscribe(
            Game game,
            Class<T> eventClass,
            Consumer<? super T> consumer
    ) {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(eventClass, "eventClass");
        Objects.requireNonNull(consumer, "consumer");

        var perGame = EVENTS.computeIfAbsent(game, g -> new ConcurrentHashMap<>());
        var rawList = perGame.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>());

        CopyOnWriteArrayList<Consumer<? super T>> list = (CopyOnWriteArrayList<Consumer<? super T>>) rawList;

        if (!list.contains(consumer)) {
            list.add(consumer);
        }
        return new Subscription(game, eventClass, consumer);
    }

    public static <T extends Event> Subscription subscribeOnce(
            Game game,
            Class<T> eventClass,
            Consumer<? super T> consumer
    ) {
        final Consumer<? super T>[] holder = new Consumer[1];
        Consumer<T> wrapper = e -> {
            try {
                consumer.accept(e);
            } finally {
                unsubscribe(game, eventClass, holder[0]);
            }
        };
        holder[0] = wrapper;
        return subscribe(game, eventClass, wrapper);
    }

    public static <T extends Event> void unsubscribe(
            Game game,
            Class<T> eventClass,
            Consumer<? super T> consumer
    ) {
        var perGame = EVENTS.get(game);
        if (perGame == null) return;

        var rawList = perGame.get(eventClass);
        if (rawList == null) return;

        CopyOnWriteArrayList<Consumer<? super T>> list =
                (CopyOnWriteArrayList<Consumer<? super T>>) rawList;

        list.remove(consumer);
        if (list.isEmpty()) {
            perGame.remove(eventClass);
            if (perGame.isEmpty()) EVENTS.remove(game);
        }
    }

    public static <T extends Event> void unsubscribe(Game game, Class<T> eventClass) {
        var perGame = EVENTS.get(game);
        if (perGame == null) return;
        perGame.remove(eventClass);
        if (perGame.isEmpty()) EVENTS.remove(game);
    }

    public static void unsubscribeAll(Game game) {
        EVENTS.remove(game);
    }

    public static void clear() {
        EVENTS.clear();
    }

    public static <T extends Event> void publish(Game game, T event) {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(event, "event");

        var perGame = EVENTS.get(game);
        if (perGame == null) return;

        var rawList = perGame.get(event.getClass());
        if (rawList == null || rawList.isEmpty()) return;

        CopyOnWriteArrayList<Consumer<? super T>> list =
                (CopyOnWriteArrayList<Consumer<? super T>>) rawList;

        for (Consumer<? super T> c : list) {
            c.accept(event);
        }
    }

    public static boolean hasSubscribers(Game game, Class<? extends Event> eventClass) {
        var perGame = EVENTS.get(game);
        var list = perGame != null ? perGame.get(eventClass) : null;
        return list != null && !list.isEmpty();
    }

    public static int subscribersCount(Game game, Class<? extends Event> eventClass) {
        var perGame = EVENTS.get(game);
        var list = perGame != null ? perGame.get(eventClass) : null;
        return list == null ? 0 : list.size();
    }

    public record Subscription(
            Game game,
            Class<?> eventClass,
            Consumer<?> consumer
    ) {
        public <T extends Event> void unsubscribe() {
            GameEvent.unsubscribe(game, (Class<T>) eventClass, (Consumer<? super T>) consumer);
        }
    }
}
