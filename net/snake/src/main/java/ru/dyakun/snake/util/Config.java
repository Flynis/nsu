package ru.dyakun.snake.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class Config {
    protected abstract void validate();

    public static <T extends Config> T load(String filaName, Class<T> clazz) {
        try {
            Gson gson = new GsonBuilder().create();
            String input = new String(Files.readAllBytes(Paths.get(filaName)));
            T cfg = gson.fromJson(input, clazz);
            cfg.validate();
            return cfg;
        } catch (IOException | JsonSyntaxException e) {
            throw new ConfigLoadException(e);
        }
    }
}
