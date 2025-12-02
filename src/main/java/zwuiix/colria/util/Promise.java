package zwuiix.colria.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Promise<T> {
    private enum State {
        PENDING,
        FULFILLED,
        REJECTED
    }

    private State state = State.PENDING;
    private T result;
    private Throwable error;

    private final List<Consumer<T>> onThen = new ArrayList<>();
    private final List<Consumer<Throwable>> onCatch = new ArrayList<>();

    public synchronized boolean isResolved() {
        return state != State.PENDING;
    }

    public void resolve(T value) {
        List<Consumer<T>> callbacks;
        synchronized (this) {
            if (state != State.PENDING) {
                throw new IllegalStateException("Promise has already been resolved");
            }

            state = State.FULFILLED;
            this.result = value;

            callbacks = new ArrayList<>(onThen);
            onThen.clear();
            onCatch.clear();
        }

        for (Consumer<T> callback : callbacks) {
            callback.accept(value);
        }
    }

    public void reject(Throwable reason) {
        List<Consumer<Throwable>> callbacks;
        synchronized (this) {
            if (state != State.PENDING) {
                throw new IllegalStateException("Promise has already been resolved");
            }

            state = State.REJECTED;
            this.error = reason;

            callbacks = new ArrayList<>(onCatch);
            onThen.clear();
            onCatch.clear();
        }

        for (Consumer<Throwable> callback : callbacks) {
            callback.accept(reason);
        }
    }

    public Promise<T> then(Consumer<T> callback) {
        T currentResult;
        synchronized (this) {
            if (state == State.FULFILLED) {
                currentResult = result;
            } else if (state == State.PENDING) {
                onThen.add(callback);
                return this;
            } else {
                return this;
            }
        }

        callback.accept(currentResult);
        return this;
    }

    public Promise<T> onCatch(Consumer<Throwable> callback) {
        Throwable currentError;
        synchronized (this) {
            if (state == State.REJECTED) {
                currentError = error;
            } else if (state == State.PENDING) {
                onCatch.add(callback);
                return this;
            } else {
                return this;
            }
        }

        callback.accept(currentError);
        return this;
    }
}
