package me.kenzierocks.converse.gsonadapt;

import java.lang.reflect.Type;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class ImmutableListDeserializer
        implements JsonDeserializer<ImmutableList<?>> {

    @Override
    public ImmutableList<?> deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        @SuppressWarnings("unchecked")
        TypeToken<ImmutableList<?>> tt =
                (TypeToken<ImmutableList<?>>) TypeToken.of(typeOfT);
        return ImmutableList.copyOf((List<?>) context.deserialize(json,
                tt.getSupertype(List.class).getType()));
    }

}
