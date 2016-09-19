package org.hildan.fxlog.config;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

import org.hildan.fxgson.FxGson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Creates a pre-configured {@link GsonBuilder} or {@link Gson} to serialize FX Log's configuration.
 */
class ConfigGson {

    static Gson create() {
        return builder().create();
    }

    static GsonBuilder builder() {
        return FxGson.fullBuilder().registerTypeAdapter(Pattern.class, new PatternSerializer());
    }

    private static class PatternSerializer implements JsonSerializer<Pattern>, JsonDeserializer<Pattern> {
        @Override
        public JsonElement serialize(Pattern pattern, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(pattern.pattern());
        }

        @Override
        public Pattern deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return Pattern.compile(json.getAsString());
        }
    }
}
