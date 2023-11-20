package ru.dyakun.api.service;

import ru.dyakun.api.util.Config;

public class ServiceConfig extends Config {
    private String graphhopperAuthKey;
    private String openWeatherMapKey;
    private int limit;

    public String getGraphhopperAuthKey() {
        return graphhopperAuthKey;
    }

    public String getOpenWeatherMapKey() {
        return openWeatherMapKey;
    }


    public int getLimit() {
        return limit;
    }

    @Override
    protected void validate() {
        if(graphhopperAuthKey.isBlank()) {
            throw new IllegalStateException("Graphhopper auth key is empty");
        }
        if(openWeatherMapKey.isBlank()) {
            throw new IllegalStateException("OpenWeatherMap key is empty");
        }
        if(limit < 1) {
            throw new IllegalStateException("Limit must be > 0");
        }
    }
}
