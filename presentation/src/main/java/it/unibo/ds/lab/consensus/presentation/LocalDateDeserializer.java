package it.unibo.ds.lab.consensus.presentation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.Month;

public class LocalDateDeserializer implements JsonDeserializer<LocalDate> {
    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            var object = json.getAsJsonObject();
            var year = object.get("year").getAsInt();
            var monthString = object.get("month").getAsString();
            var month = Month.valueOf(monthString.toUpperCase());
            var day = object.get("day").getAsInt();
            return LocalDate.of(year, month, day);
        } catch (IllegalArgumentException | ClassCastException | NullPointerException e) {
            throw new JsonParseException("Invalid date: " + json, e);
        }
    }
}
