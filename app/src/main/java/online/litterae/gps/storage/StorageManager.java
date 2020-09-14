package online.litterae.gps.storage;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import online.litterae.gps.application.App;

public class StorageManager {
    private static StorageManager instance;
    private MyLocationDao myLocationDao;
    private BehaviorSubject<List<MyLocation>> locationsProvider;

    private StorageManager() {
        myLocationDao = App.getDatabase().myLocationDao();
        locationsProvider = BehaviorSubject.createDefault(new ArrayList<>());
        myLocationDao.getLocations()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(locationsProvider);
    }

    public static StorageManager getInstance() {
        if (instance == null) {
            instance = new StorageManager();
        }
        return instance;
    }

    public BehaviorSubject<List<MyLocation>> getLocationsProvider() {
        return locationsProvider;
    }

    public void addLocation(MyLocation myLocation) {
        Completable.fromAction(() -> myLocationDao.insert(myLocation))
                .subscribeOn(Schedulers.io()).subscribe();
    }

    public void wipeData() {
        Completable.fromAction(() -> myLocationDao.wipeData())
                .subscribeOn(Schedulers.io()).subscribe();
    }
}