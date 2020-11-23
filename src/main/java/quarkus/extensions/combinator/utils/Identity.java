package quarkus.extensions.combinator.utils;

import java.util.function.Function;

@FunctionalInterface
public interface Identity<T> extends Function<T, T> {

}
