package online.litterae.gps.storage;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MyLocation {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public double latitude;
    public double longitude;

    public MyLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
