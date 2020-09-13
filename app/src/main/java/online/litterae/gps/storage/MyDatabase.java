package online.litterae.gps.storage;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {MyLocation.class}, version = 1)
public abstract class MyDatabase extends RoomDatabase {
    public abstract MyLocationDao myLocationDao();
}
