package it.unibo.ds.lab.consensus.presentation;

import com.google.gson.*;
import it.unibo.ds.lab.consensus.client.Message;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class MessageDeserializer implements JsonDeserializer<Message> {
    @Override
    public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            var object = json.getAsJsonObject();
            String username = getPropertyAsString(object, "username");
            String body = getPropertyAsString(object, "body");
            LocalDate date = getPropertyAs(object, "date", LocalDate.class, context);
            LocalTime time = getPropertyAs(object, "time", LocalTime.class, context);
            return new Message(username, Objects.requireNonNull(body).getBytes(), date, time);
        } catch (ClassCastException e) {
            throw new JsonParseException("Invalid user: " + json, e);
        }
    }

    private <T> T getPropertyAs(JsonObject object, String name, Class<T> type, JsonDeserializationContext context) {
        if (object.has(name)) {
            JsonElement value = object.get(name);
            if (value.isJsonNull()) return null;
            return context.deserialize(value, type);
        }
        return null;
    }

    private String getPropertyAsString(JsonObject object, String name) {
        if (object.has(name)) {
            JsonElement value = object.get(name);
            if (value.isJsonNull()) return null;
            return value.getAsString();
        }
        return null;
    }
}
