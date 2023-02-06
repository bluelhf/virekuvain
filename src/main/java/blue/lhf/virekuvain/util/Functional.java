package blue.lhf.virekuvain.util;

import java.util.function.*;

public class Functional {
    private Functional() {

    }

    public static <T> Function<T, Void> returnNull(final Consumer<T> consumer) {
        return (t) -> {
            consumer.accept(t);
            return null;
        };
    }

    public static <T, R> Predicate<T> notNull(final Function<T, R> function) {
        return t -> function.apply(t) != null;
    }
}
