package online.litterae.gps.utils;

import java.util.List;

import online.litterae.gps.storage.MyLocation;

import static online.litterae.gps.utils.Const.LOCATION_GIVEN_LATITUDE;
import static online.litterae.gps.utils.Const.LOCATION_GIVEN_LONGITUDE;

public class Calculator {
    public static int getMinDistance(List<MyLocation> locations) {
        return locations.stream()
                .map(location -> getDistanceInMeters(
                        location.getLatitude(), LOCATION_GIVEN_LATITUDE,
                        location.getLongitude(), LOCATION_GIVEN_LONGITUDE))
                .min(Integer::compareTo).orElse(-1);
    }

    public static int getMaxDistance(List<MyLocation> locations) {
        return locations.stream()
                .map(location -> getDistanceInMeters(
                        location.getLatitude(), LOCATION_GIVEN_LATITUDE,
                        location.getLongitude(), LOCATION_GIVEN_LONGITUDE))
                .max(Integer::compareTo).orElse(-1);
    }

    public static int getDistanceInMeters(double latitude1, double latitude2,
                                           double longitude1, double longitude2) {
        longitude1 = Math.toRadians(longitude1);
        longitude2 = Math.toRadians(longitude2);
        latitude1 = Math.toRadians(latitude1);
        latitude2 = Math.toRadians(latitude2);

        double dLongitude = longitude2 - longitude1;
        double dLatitude = latitude2 - latitude1;
        double haversine = Math.pow(Math.sin(dLatitude / 2), 2)
                + Math.cos(latitude1) * Math.cos(latitude2)
                * Math.pow(Math.sin(dLongitude / 2),2);
        double distance = 2 * Math.asin(Math.sqrt(haversine));
        double earthRadius = 6371;
        return (int)(distance * earthRadius * 1000);
    }
}