package org.hildan.fxlog.config;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
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

/**
 * Creates a pre-configured {@link GsonBuilder} that handles properly JavaFX properties.
 * <p>
 * Basically, this class contains custom serializers for properties. These custom serializers make sure the serialized
 * version of the property simply contains the value of the property.
 */
class FxGson {

    /**
     * Creates a pre-configured {@link GsonBuilder} that handles properly JavaFX properties.
     *
     * @return a pre-configured {@link GsonBuilder} that handles properly JavaFX properties.
     */
    static GsonBuilder builder() {
        return new GsonBuilder().registerTypeAdapter(ObservableList.class, new ObservableListCreator())
                                .registerTypeAdapter(DoubleProperty.class, new DoublePropertySerializer())
                                .registerTypeAdapter(DoubleProperty.class, new DoublePropertyDeserializer())
                                .registerTypeAdapter(BooleanProperty.class, new BooleanPropertySerializer())
                                .registerTypeAdapter(BooleanProperty.class, new BooleanPropertyDeserializer())
                                .registerTypeAdapter(IntegerProperty.class, new IntegerPropertySerializer())
                                .registerTypeAdapter(IntegerProperty.class, new IntegerPropertyDeserializer())
                                .registerTypeAdapter(Property.class, new PropertySerializer())
                                .registerTypeAdapter(Property.class, new PropertyDeserializer())
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

    private static class IntegerPropertySerializer implements JsonSerializer<IntegerProperty> {
        @Override
        public JsonElement serialize(IntegerProperty intProp, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(intProp.get());
        }
    }

    private static class IntegerPropertyDeserializer implements JsonDeserializer<IntegerProperty> {
        @Override
        public IntegerProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            return new SimpleIntegerProperty(json.getAsInt());
        }
    }

    private static class PropertySerializer implements JsonSerializer<Property<?>> {
        @Override
        public JsonElement serialize(Property<?> doubleProp, Type type, JsonSerializationContext context) {
            return context.serialize(doubleProp.getValue());
        }
    }

    private static class PropertyDeserializer implements JsonDeserializer<Property<?>> {
        @Override
        public Property<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            Type typeParam = ((ParameterizedType)typeOfT).getActualTypeArguments()[0];
            Object obj = context.deserialize(json, typeParam);
            return new SimpleObjectProperty<>(obj);
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
