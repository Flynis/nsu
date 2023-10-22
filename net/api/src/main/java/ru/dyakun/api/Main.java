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
                service.getWeather();
            } catch (ServiceException e) {
                System.out.println("Calc weather failed");
            }
        }).thenRunAsync(() -> {
            try {
                var attractions = service.searchAttractions();
                System.out.println("Found " + attractions.size() + " attractions");
            } catch (ServiceException e) {
                System.out.println("Search attractions failed");
            }
        }).thenRun(() -> {
            try {
                var attractions = service.getFoundAttractions();
                for(var id : attractions) {
                    service.getAttractionDescById(id);
                    System.out.println();
                }
            } catch (ServiceException e) {
                System.out.println("Search attraction description failed");
            }
        });
        try {
            futures.get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Fatal error " + e.getMessage());
        }
    }
}