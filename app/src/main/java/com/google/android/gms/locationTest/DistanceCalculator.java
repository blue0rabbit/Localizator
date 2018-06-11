package com.google.android.gms.locationTest;

import com.google.android.gms.maps.model.LatLng;
/**
 * Created by blue rabbit on 2018-01-17.
 */
public class DistanceCalculator {
    private static final int EARTH_RADIUS = 6371;
//prosta funkcja sluzaca do obliczania odleglosci
    public static double calculationByDistance(LatLng first, LatLng second) {
        double latitudeDifference = (Math.toRadians(Math.abs(second.latitude - first.latitude)));
        System.out.println(latitudeDifference);
        double longitudeDifference = Math.toRadians(Math.abs(second.longitude - first.longitude));
        System.out.println(longitudeDifference);

        double rough = Math.pow(Math.sin(latitudeDifference / 2), 2)
                + Math.cos(Math.toRadians(first.latitude))
                * Math.cos(Math.toRadians(second.latitude)) * Math.pow(Math.sin(longitudeDifference / 2), 2);

        return 2 * EARTH_RADIUS * Math.asin(Math.sqrt(rough));
    }
}
