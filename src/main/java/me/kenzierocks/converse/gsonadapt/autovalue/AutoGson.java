package me.kenzierocks.converse.gsonadapt.autovalue;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.auto.value.AutoValue;

/**
 * Marks an {@link AutoValue @AutoValue}-annotated type for proper Gson
 * serialization.
 * <p>
 * This annotation is needed because the {@linkplain Retention retention} of
 * {@code @AutoValue} does not allow reflection at runtime.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface AutoGson {
}
