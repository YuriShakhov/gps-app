package online.litterae.gps;

import android.location.Location;

import org.junit.Test;

import online.litterae.gps.storage.MyLocation;

import static org.junit.Assert.*;

public class FormatLocationTest {

    @Test
    public void latLonFormatCorrect() {
        assertEquals("1.23E,1.23N", format(1.23, 1.23));
        assertEquals("1.23W,1.23S", format(-1.23, -1.23));
    }

    private String format(double latitude, double longitude) {
        MyLocation myLocation = new MyLocation(latitude, longitude);
        return MainActivity.formatLocation(myLocation);
    }
}