package ru.dyakun.api.service;

import com.google.gson.annotations.SerializedName;

public class Location {
    public static class Point {
        private double lat;
        private double lng;

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }
    }

    private Point point;
    @SerializedName("osm_id")
    private String osmID;
    @SerializedName("osm_type")
    private String osmType;
    @SerializedName("osm_key")
    private String osmKey;
    private String name;
    private String country;
    private String city;
    private String state;
    private String street;
    @SerializedName("housenumber")
    private String houseNumber;
    @SerializedName("postcode")
    private String postCode;

    public Point getPoint() {
        return point;
    }

    public String getOsmID() {
        return osmID;
    }

    public String getOsmType() {
        return osmType;
    }

    public String getOsmKey() {
        return osmKey;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getStreet() {
        return street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getPostCode() {
        return postCode;
    }
}
