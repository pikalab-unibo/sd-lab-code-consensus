package it.unibo.ds.lab.consensus.presentation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalTime;

public class LocalTimeSerializer implements JsonSerializer<LocalTime> {

    @Override
    public JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
        var object = new JsonObject();
        object.addProperty("hour", src.getHour());
        object.addProperty("minute", src.getMinute());
        object.addProperty("second", src.getSecond());

        return object;
    }
}
