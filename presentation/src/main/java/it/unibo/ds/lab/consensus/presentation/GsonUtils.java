package it.unibo.ds.lab.consensus.presentation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unibo.ds.lab.consensus.client.Message;

import java.time.LocalDate;
import java.time.LocalTime;

public class GsonUtils {
    public static Gson createGson() {
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .serializeNulls()
                .registerTypeAdapter(Message.class, new MessageSerializer())
                .registerTypeAdapter(Message.class, new MessageDeserializer())
                .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
                .registerTypeAdapter(LocalTime.class, new LocalTimeSerializer())
                .registerTypeAdapter(LocalTime.class, new LocalTimeDeserializer())
                .create();
    }
}
