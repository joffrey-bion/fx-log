package org.hildan.fxlog.config;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class FxGson {

    public static GsonBuilder builder() {
        return new GsonBuilder().registerTypeAdapter(ObservableList.class, new ObservableListCreator())
                                .registerTypeAdapter(DoubleProperty.class, new DoublePropertySerializer())
                                .registerTypeAdapter(DoubleProperty.class, new DoublePropertyDeserializer())
                                .registerTypeAdapter(BooleanProperty.class, new BooleanPropertySerializer())
                                .registerTypeAdapter(BooleanProperty.class, new BooleanPropertyDeserializer())
                                .registerTypeAdapter(Pattern.class, new PatternSerializer())
                                .registerTypeAdapter(Pattern.class, new PatternDeserializer());
    }

    private static class ObservableListCreator implements InstanceCreator<ObservableList<?>> {

        public ObservableList<?> createInstance(Type type) {
            // No need to use a parametrized list since the actual instance will have the raw type anyway.
            return FXCollections.observableArrayList();
        }
    }

    private static class DoublePropertySerializer implements JsonSerializer<DoubleProperty> {
        @Override
        public JsonElement serialize(DoubleProperty doubleProp, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(doubleProp.get());
        }
    }

    private static class DoublePropertyDeserializer implements JsonDeserializer<DoubleProperty> {
        @Override
        public DoubleProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            return new SimpleDoubleProperty(json.getAsDouble());
        }
    }

    private static class BooleanPropertySerializer implements JsonSerializer<BooleanProperty> {
        @Override
        public JsonElement serialize(BooleanProperty booleanProp, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(booleanProp.get());
        }
    }

    private static class BooleanPropertyDeserializer implements JsonDeserializer<BooleanProperty> {
        @Override
        public BooleanProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            return new SimpleBooleanProperty(json.getAsBoolean());
        }
    }

    private static class PatternSerializer implements JsonSerializer<Pattern> {
        @Override
        public JsonElement serialize(Pattern pattern, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(pattern.toString());
        }
    }

    private static class PatternDeserializer implements JsonDeserializer<Pattern> {
        @Override
        public Pattern deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            return Pattern.compile(json.getAsString());
        }
    }
}
