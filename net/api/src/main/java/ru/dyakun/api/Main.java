package ru.dyakun.api;

import ru.dyakun.api.service.Location;
import ru.dyakun.api.service.Service;
import ru.dyakun.api.service.ServiceConfig;
import ru.dyakun.api.service.ServiceException;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) {
        var config = ServiceConfig.load("service.cfg", ServiceConfig.class);
        var service = new Service(config);
        var in = new Scanner(System.in);
        CompletableFuture<Void> futures = CompletableFuture.runAsync(()-> {
            try {
                System.out.println("Enter area name");
                String area = in.next();
                List<Location> locations = service.searchCoordinates(area);
                for(int i = 0; i < locations.size(); i++) {
                    System.out.printf("%d: %s%n", i, locations.get(i).getName());
                }
                System.out.println("Choose location");
                int number = in.nextInt();
                service.chooseLocation(number);
            } catch (ServiceException e) {
                System.out.println("Get location coordinates failed");
            }
        }).thenRun(() -> {
            try {
                CompletableFuture<Void> searchAttractions = CompletableFuture.supplyAsync(() -> {
                    try {
                        var attractions = service.searchAttractions();
                        System.out.println("Found " + attractions.size() + " attractions");
                        for(var id : attractions) {
                            service.getAttractionDescById(id);
                            System.out.println();
                        }
                    } catch (ServiceException e) {
                        System.out.println("Search attractions failed");
                    }
                    return null;
                });

                CompletableFuture<Void> weather = CompletableFuture.supplyAsync(() -> {
                    try {
                        service.getWeather();
                    } catch (ServiceException e) {
                        System.out.println("Calc weather failed");
                    }
                    return null;
                });

                CompletableFuture<Void> routine = CompletableFuture.allOf(searchAttractions, weather);
                routine.get();
            } catch (ExecutionException | InterruptedException e) {
                System.out.println("Fatal error " + e.getMessage());
            }
        });

        try {
            futures.get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Fatal error " + e.getMessage());
        }
    }
}
