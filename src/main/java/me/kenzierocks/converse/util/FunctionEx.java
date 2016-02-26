package me.kenzierocks.converse.util;

import java.util.Objects;
import java.util.function.Function;

import com.google.common.base.Throwables;

/**
 * Represents a function that accepts one argument and produces a result,
 * optionally throwing an exception.
 *
 * <p>
 * This is a functional interface whose functional method is
 * {@link #apply(Object)}.
 *
 * @param <T>
 *            the type of the input to the function
 * @param <R>
 *            the type of the result of the function
 * @param <E>
 *            the type of the exception to be thrown
 */
@FunctionalInterface
public interface FunctionEx<T, R, E extends Throwable> {

    /**
     * Applies this function to the given argument.
     *
     * @param t
     *            the function argument
     * @return the function result
     */
    R apply(T t) throws E;

    /**
     * Returns a composed function that first applies the {@code before}
     * function to its input, and then applies this function to the result. If
     * evaluation of either function throws an exception, it is relayed to the
     * caller of the composed function.
     *
     * @param <V>
     *            the type of input to the {@code before} function, and to the
     *            composed function
     * @param before
     *            the function to apply before this function is applied
     * @return a composed function that first applies the {@code before}
     *         function and then applies this function
     * @throws NullPointerException
     *             if before is null
     *
     * @see #andThen(Function)
     */
    default <V> FunctionEx<V, R, E>
            compose(FunctionEx<? super V, ? extends T, ? extends E> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    /**
     * Returns a composed function that first applies this function to its
     * input, and then applies the {@code after} function to the result. If
     * evaluation of either function throws an exception, it is relayed to the
     * caller of the composed function.
     *
     * @param <V>
     *            the type of output of the {@code after} function, and of the
     *            composed function
     * @param after
     *            the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     *         applies the {@code after} function
     * @throws NullPointerException
     *             if after is null
     *
     * @see #compose(Function)
     */
    default <V> FunctionEx<T, V, E>
            andThen(FunctionEx<? super R, ? extends V, ? extends E> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    default Function<T, R> wrapExceptions() {
        return t -> {
            try {
                return apply(t);
            } catch (Throwable e) {
                // If apply throws other exceptions, oh well.
                // Can't do much about it.
                throw Throwables.propagate(e);
            }
        };
    }

    /**
     * Returns a function that always returns its input argument.
     *
     * @param <T>
     *            the type of the input and output objects to the function
     * @return a function that always returns its input argument
     */
    static <T> FunctionEx<T, T, RuntimeException> identity() {
        return t -> t;
    }

    static <T, R, E extends Throwable> FunctionEx<T, R, E>
            throwing(E exception) {
        Objects.requireNonNull(exception);
        return t -> {
            throw exception;
        };
    }

    static <T, R> FunctionEx<T, R, RuntimeException>
            of(Function<T, R> function) {
        Objects.requireNonNull(function);
        return function::apply;
    }

}
