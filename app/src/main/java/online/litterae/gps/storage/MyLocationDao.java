package online.litterae.gps.storage;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Observable;

@Dao
public interface MyLocationDao {
    @Query("SELECT * FROM MyLocation")
    Observable<List<MyLocation>> getLocations();

    @Query("DELETE FROM MyLocation")
    void wipeData();

    @Insert
    void insert(MyLocation myLocation);
}