package org.hildan.fxlog.config;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

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
        // serialization of nulls is necessary to have properties with null values deserialized properly
        return new GsonBuilder().serializeNulls()
                                .registerTypeAdapter(ObservableList.class, new ObservableListCreator())
                                .registerTypeAdapter(StringProperty.class, new StringPropertySerializer())
                                .registerTypeAdapter(DoubleProperty.class, new DoublePropertySerializer())
                                .registerTypeAdapter(BooleanProperty.class, new BooleanPropertySerializer())
                                .registerTypeAdapter(IntegerProperty.class, new IntegerPropertySerializer())
                                .registerTypeAdapter(Property.class, new PropertySerializer())
                                .registerTypeAdapter(Color.class, new ColorSerializer())
                                .registerTypeAdapter(Pattern.class, new PatternSerializer());
    }

    private static class ObservableListCreator implements InstanceCreator<ObservableList<?>> {

        public ObservableList<?> createInstance(Type type) {
            // No need to use a parametrized list since the actual instance will have the raw type anyway.
            return FXCollections.observableArrayList();
        }
    }

    private static class PropertySerializer implements JsonSerializer<Property<?>>, JsonDeserializer<Property<?>> {

        private static final String NULL_PLACEHOLDER = "null";

        @Override
        public JsonElement serialize(Property<?> property, Type type, JsonSerializationContext context) {
            Object value = property.getValue();
            // FIXME this is the only workaround I could think of so far for this Gson bug:
            // https://github.com/google/gson/issues/171
            // Ongoing stackoverflow to tackle this problem
            // http://stackoverflow.com/questions/35260490/gson-custom-deserializer-not-called-for-null
            return context.serialize(value != null ? value : NULL_PLACEHOLDER);
        }

        @Override
        public Property<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            if (isNullPlaceholder(json)) {
                return new SimpleObjectProperty<>(null);
            }
            Type typeParam = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];
            Object obj = context.deserialize(json, typeParam);
            return new SimpleObjectProperty<>(obj);
        }

        private static boolean isNullPlaceholder(JsonElement json) {
            try {
                return json.isJsonPrimitive() && NULL_PLACEHOLDER.equals(json.getAsString());
            } catch (ClassCastException e) {
                return false; // not a string, so definitely not the placeholder
            }
        }
    }

    private static class StringPropertySerializer implements JsonSerializer<StringProperty>,
            JsonDeserializer<StringProperty> {

        private static final int NULL_PLACEHOLDER = 0;

        @Override
        public JsonElement serialize(StringProperty property, Type type, JsonSerializationContext context) {
            if (property.get() != null) {
                return new JsonPrimitive(property.get());
            } else {
                return new JsonPrimitive(NULL_PLACEHOLDER);
            }
        }

        @Override
        public StringProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            if (isNullPlaceholder(json)) {
                return new SimpleStringProperty(null);
            }
            return new SimpleStringProperty(json.getAsString());
        }

        private static boolean isNullPlaceholder(JsonElement json) {
            try {
                return json.isJsonPrimitive() && NULL_PLACEHOLDER == json.getAsInt();
            } catch (ClassCastException | NumberFormatException e) {
                return false; // not an int, so definitely not the placeholder
            }
        }
    }

    private static class DoublePropertySerializer implements JsonSerializer<DoubleProperty>,
            JsonDeserializer<DoubleProperty> {
        @Override
        public JsonElement serialize(DoubleProperty property, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(property.get());
        }

        @Override
        public DoubleProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            return new SimpleDoubleProperty(json.getAsDouble());
        }
    }

    private static class BooleanPropertySerializer implements JsonSerializer<BooleanProperty>,
            JsonDeserializer<BooleanProperty> {
        @Override
        public JsonElement serialize(BooleanProperty property, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(property.get());
        }

        @Override
        public BooleanProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            return new SimpleBooleanProperty(json.getAsBoolean());
        }
    }

    private static class IntegerPropertySerializer implements JsonSerializer<IntegerProperty>,
            JsonDeserializer<IntegerProperty> {
        @Override
        public JsonElement serialize(IntegerProperty property, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(property.get());
        }

        @Override
        public IntegerProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            return new SimpleIntegerProperty(json.getAsInt());
        }
    }

    private static class ColorSerializer implements JsonSerializer<Color>, JsonDeserializer<Color> {
        @Override
        public JsonElement serialize(Color color, Type type, JsonSerializationContext context) {
            return new JsonPrimitive("#" + color.toString().substring(2));
        }

        @Override
        public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            return Color.web(json.getAsString());
        }
    }

    private static class PatternSerializer implements JsonSerializer<Pattern>, JsonDeserializer<Pattern> {
        @Override
        public JsonElement serialize(Pattern pattern, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(pattern.toString());
        }

        @Override
        public Pattern deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {
            return Pattern.compile(json.getAsString());
        }
    }
}
