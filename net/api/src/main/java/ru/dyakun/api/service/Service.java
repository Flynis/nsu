package ru.dyakun.api.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Service {
    private final ServiceConfig config;
    private final HttpClient client;
    public Service(ServiceConfig config) {
        this.config = config;
        client = HttpClient.newHttpClient();
    }

    private List<Location> locations;
    private Location chosenLocation;
    private List<Integer> foundAttractions;
    public void chooseLocation(int number) {
        if(number < 0 || number > locations.size()) {
            throw new IllegalArgumentException("Illegal location number");
        }
        chosenLocation = locations.get(number);
    }

    public List<Integer> getFoundAttractions() {
        return foundAttractions;
    }

    private static class GraphResponse {
        @SerializedName("hits")
        public List<Location> locations;
        public long took;
    }

    public List<Location> searchCoordinates(String area) throws ServiceException {
        System.out.println("Search coordinates");
        URI uri = URI.create("https://graphhopper.com/api/1/geocode?q="+area
                +"&limit="+config.getLimit()
                +"&key="+config.getGraphhopperAuthKey());
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        var responseFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        try {
            HttpResponse<String> response = responseFuture.get();
            if(response.statusCode() != 200) {
                System.out.println("Search failed " + response.statusCode());
                return null;
            }
            //System.out.println("response "+response.body());
            Gson gson = new GsonBuilder().create();
            GraphResponse responseObj = gson.fromJson(response.body(), GraphResponse.class);
            locations = responseObj.locations;
            return locations;
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Search failed", e);
        }
    }

    public void getWeather() throws ServiceException {
        System.out.println("Calculate weather for " + chosenLocation.getName());
        URI uri = URI.create("https://api.openweathermap.org/data/2.5/weather?lat="
                +chosenLocation.getPoint().getLat()
                +"&lon="+chosenLocation.getPoint().getLng()
                +"&appid="+config.getOpenWeatherMapKey()+"&units=metric");
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        var responseFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        try {
            HttpResponse<String> response = responseFuture.get();
            //System.out.println("response "+response.body());
            if(response.statusCode() != 200) {
                System.out.println("Search failed " + response.statusCode());
                return;
            }
            System.out.println("Weather:");
            JSONObject obj = new JSONObject(response.body());
            JSONArray weather = obj.getJSONArray("weather");
            int n = weather.length();
            for (int i = 0; i < n; i++) {
                JSONObject item = weather.getJSONObject(i);
                System.out.println(item.getString("description"));
            }
            JSONObject main = obj.getJSONObject("main");
            System.out.println("Temperature " + main.getBigDecimal("temp"));
            System.out.println("Feels like " + main.getBigDecimal("feels_like"));
            System.out.println("Min temperature " + main.getBigDecimal("temp_min"));
            System.out.println("Max temperature " + main.getBigDecimal("temp_max"));
            JSONObject wind = obj.getJSONObject("wind");
            System.out.println("Wind " + wind.getBigDecimal("speed") + " m/s");
        } catch (ExecutionException | InterruptedException e) {
            throw new ServiceException("Search weather failed", e);
        }
    }

    public List<Integer> searchAttractions() throws ServiceException {
        System.out.println("Search attractions");
        URI uri = URI.create("https://kudago.com/public-api/v1.4/places/?lat="
                +chosenLocation.getPoint().getLat()
                +"&lon="+chosenLocation.getPoint().getLng()
                +"&radius=1000");
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        var responseFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        try {
            HttpResponse<String> response = responseFuture.get();
            //System.out.println("response "+response.body());
            foundAttractions = new ArrayList<>();
            if(response.statusCode() != 200) {
                System.out.println("Search failed " + response.statusCode());
                return foundAttractions;
            }
            JSONObject obj = new JSONObject(response.body());
            JSONArray results = obj.getJSONArray("results");
            int n = results.length();
            for (int i = 0; i < n; ++i) {
                JSONObject item = results.getJSONObject(i);
                foundAttractions.add(item.getInt("id"));
            }
            return foundAttractions;
        } catch (ExecutionException | InterruptedException e) {
            throw new ServiceException("Search attractions failed", e);
        }
    }

    public void getAttractionDescById(int id) throws ServiceException {
        System.out.println("Search description for " + id);
        URI uri = URI.create("https://kudago.com/public-api/v1.4/places/"+id+"/");
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        var responseFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        try {
            HttpResponse<String> response = responseFuture.get();
            //System.out.println("response "+response.body());
            if(response.statusCode() != 200) {
                System.out.println("Search failed " + response.statusCode());
                return;
            }
            JSONObject obj = new JSONObject(response.body());
            System.out.println("Id " + obj.getInt("id"));
            System.out.println("Title " + obj.getString("title"));
            System.out.println("Address " + obj.getString("address"));
            System.out.println("Timetable " + obj.getString("timetable"));
            System.out.println("Phone " + obj.getString("phone"));
            //System.out.println("Text " + obj.getString("body_text"));
            System.out.println("Description: " + obj.getString("description"));
            System.out.println("Site " + obj.getString("site_url"));
            System.out.println("Subway " + obj.getString("subway"));
        } catch (ExecutionException | InterruptedException e) {
            throw new ServiceException("Search attraction description failed", e);
        }
    }
}
