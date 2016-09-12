package me.kenzierocks.converse.gsonadapt.autovalue;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public final class AutoValueAdapterFactory implements TypeAdapterFactory {

    private static final class SpecialiedAutoValueAdapter<T> extends TypeAdapter<T> {

        private final TypeAdapter<T> delegate;

        private SpecialiedAutoValueAdapter(TypeAdapter<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            this.delegate.write(out, value);
        }

        @Override
        public T read(JsonReader in) throws IOException {
            T t = this.delegate.read(in);
            try {
                // This look silly, but if hashCode succeeds then an AV class is
                // in a valid state. If it throws NPE, then we set a non
                // @Nullable field to null. Uh oh!
                t.hashCode();
            } catch (NullPointerException invalidObjectState) {
                throw new IllegalStateException("Input JSON had an invalid format, some fields ended up null.",
                        invalidObjectState);
            }
            return t;
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();
        if (!rawType.isAnnotationPresent(AutoGson.class)) {
            return null;
        }

        String packageName = rawType.getPackage().getName();
        String className = rawType.getName().substring(packageName.length() + 1).replace('$', '_');
        String autoValueName = packageName + ".AutoValue_" + className;

        try {
            // AutoValue_T instanceof T
            Class<T> autoValueType = (Class<T>) Class.forName(autoValueName);
            return new SpecialiedAutoValueAdapter<T>(gson.getAdapter(autoValueType));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load AutoValue type " + autoValueName, e);
        }
    }

}
