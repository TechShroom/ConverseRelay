package me.kenzierocks.converse.gsonadapt;

import java.lang.reflect.Type;
import java.util.OptionalInt;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public enum OptionalIntAdapter
        implements JsonSerializer<OptionalInt>, JsonDeserializer<OptionalInt> {

    INSTANCE;

    @Override
    public OptionalInt deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        if (JsonNull.INSTANCE.equals(json)) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(json.getAsInt());
    }

    @Override
    public JsonElement serialize(OptionalInt src, Type typeOfSrc,
            JsonSerializationContext context) {
        return src.isPresent() ? new JsonPrimitive(src.getAsInt())
                : JsonNull.INSTANCE;
    }

}
