package com.example.todoapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * JSON serialization and deserialization utility class.
 *
 * @author StudentMapper
 * @version 1.0
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /** Private constructor — utility class, not instantiable. */
    private JsonUtils() {
    }

    /**
     * Serialize object to JSON.
     *
     * @param o object to serialize.
     * @return JSON representation of the object.
     * @throws JsonProcessingException if serialization fails.
     */
    public static String serialize(Object o) throws JsonProcessingException {
        return MAPPER.writeValueAsString(o);
    }

    /**
     * Deserialize JSON to Java object.
     *
     * @param json  JSON string to deserialize.
     * @param clazz target Java type.
     * @param <T>   type of the deserialized object.
     * @return deserialized object.
     * @throws IOException if deserialization fails.
     */
    public static <T> T deserialize(String json, Class<T> clazz) throws IOException {
        return MAPPER.readValue(json, clazz);
    }
}