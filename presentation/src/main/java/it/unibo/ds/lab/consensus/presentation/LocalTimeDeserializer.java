package it.unibo.ds.lab.consensus.presentation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.LocalTime;

public class LocalTimeDeserializer implements JsonDeserializer<LocalTime> {
    @Override
    public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            var object = json.getAsJsonObject();
            var hour = object.get("hour").getAsInt();
            var minute = object.get("minute").getAsInt();
            var second = object.get("second").getAsInt();
            return LocalTime.of(hour, minute, second);
        } catch (IllegalArgumentException | ClassCastException | NullPointerException e) {
            throw new JsonParseException("Invalid date: " + json, e);
        }
    }
}
