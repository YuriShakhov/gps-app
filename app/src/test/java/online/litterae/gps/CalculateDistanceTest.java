package online.litterae.gps;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import online.litterae.gps.storage.MyLocation;
import online.litterae.gps.utils.Calculator;

import static online.litterae.gps.utils.Const.LOCATION_GIVEN_LATITUDE;
import static online.litterae.gps.utils.Const.LOCATION_GIVEN_LONGITUDE;
import static org.junit.Assert.*;

public class CalculateDistanceTest {
    @Test
    public void distanceIsPositive() {
        List<MyLocation> locations = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            double latitude = random.nextInt(180) - 90;
            double longitude = random.nextInt(180) - 90;
            int distance = Calculator.getDistanceInMeters(latitude, LOCATION_GIVEN_LATITUDE,
                    longitude, LOCATION_GIVEN_LONGITUDE);
            assertTrue(distance >= 0);
            locations.add(new MyLocation(latitude, longitude));
        }
        assertTrue(Calculator.getMinDistance(locations) >= 0);
        assertTrue(Calculator.getMaxDistance(locations) >= 0);
    }

    @Test
    public void singleLocationMinEqualsMax() {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            double latitude = random.nextInt(180) - 90;
            double longitude = random.nextInt(180) - 90;
            List<MyLocation> locations = new ArrayList<>();
            locations.add(new MyLocation(latitude, longitude));
            int minDistance = Calculator.getMinDistance(locations);
            int maxDistance = Calculator.getMaxDistance(locations);
            assertEquals(minDistance, maxDistance);
        }
    }

    @Test
    public void distanceToGivenLocationIs0() {
        List<MyLocation> locations = new ArrayList<>();
        locations.add(new MyLocation(LOCATION_GIVEN_LATITUDE, LOCATION_GIVEN_LONGITUDE));
        assertEquals(Calculator.getMinDistance(locations), 0);
        assertEquals(Calculator.getMaxDistance(locations), 0);
    }
}