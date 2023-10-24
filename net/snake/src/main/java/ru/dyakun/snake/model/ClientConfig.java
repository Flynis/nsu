package ru.dyakun.snake.model;

import ru.dyakun.snake.util.Config;

public class ClientConfig extends Config {
    private String multicastGroupAddress;
    private int multicastGroupPort;
    private int announcementPeriod; // ms
    private int announcementTimeToLive; // ms

    public String getMulticastGroupAddress() {
        return multicastGroupAddress;
    }

    public int getMulticastGroupPort() {
        return multicastGroupPort;
    }

    public int getAnnouncementPeriod() {
        return announcementPeriod;
    }

    public int getAnnouncementTimeToLive() {
        return announcementTimeToLive;
    }

    @Override
    protected void validate() {
        if (multicastGroupAddress.isBlank()) {
            throw new IllegalStateException("Multicast group address is empty");
        }
        if(multicastGroupPort < 1) {
            throw new IllegalStateException("Multicast group port must be positive");
        }
        if(announcementPeriod < 100) {
            throw new IllegalStateException("Illegal announcement period");
        }
        if(announcementTimeToLive < 1000 || announcementTimeToLive > 10000) {
            throw new IllegalStateException("Illegal announcement time to live");
        }
    }

    @Override
    public String toString() {
        return String.format("ClientConfig{ multicast[%s:%d], announcement[period = %dms, ttl = %dms}",
                multicastGroupAddress,
                multicastGroupPort,
                announcementPeriod,
                announcementTimeToLive);
    }
}
