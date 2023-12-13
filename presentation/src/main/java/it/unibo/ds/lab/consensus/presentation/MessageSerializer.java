package it.unibo.ds.lab.consensus.presentation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import it.unibo.ds.lab.consensus.client.Message;

import java.lang.reflect.Type;

public class MessageSerializer implements JsonSerializer<Message> {
    @Override
    public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
        var object = new JsonObject();
        object.addProperty("username", src.getUsername());
        object.addProperty("body", src.getBody());
        object.add("date", context.serialize(src.getDate()));
        object.add("time", context.serialize(src.getTime()));
        return object;
    }
}
