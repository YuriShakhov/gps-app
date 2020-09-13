package online.litterae.gps;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import online.litterae.gps.application.App;
import online.litterae.gps.storage.MyLocation;
import online.litterae.gps.storage.StorageManager;
import online.litterae.gps.utils.Calculator;

import static online.litterae.gps.utils.Const.ACTION_MIN_MAX_DISTANCE;
import static online.litterae.gps.utils.Const.ACTION_UPDATE_GPS_LOCATIONS;
import static online.litterae.gps.utils.Const.COMMAND_CONNECT_SERVICE;
import static online.litterae.gps.utils.Const.COMMAND_SAVE_LOCATION;
import static online.litterae.gps.utils.Const.COMMAND_SHOW_MIN_MAX_DISTANCE;
import static online.litterae.gps.utils.Const.COMMAND_WIPE_DATA;
import static online.litterae.gps.utils.Const.PARAM_COMMAND;
import static online.litterae.gps.utils.Const.PARAM_LOCATIONS;
import static online.litterae.gps.utils.Const.PARAM_MAX_DISTANCE;
import static online.litterae.gps.utils.Const.PARAM_MIN_DISTANCE;

public class GpsService extends Service {
    private StorageManager storageManager = StorageManager.getInstance();
    private BehaviorSubject<List<MyLocation>> storedLocationsProvider
            = storageManager.getLocationsProvider();
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = intent.getIntExtra(PARAM_COMMAND, 0);
        switch (command) {
            case COMMAND_CONNECT_SERVICE:
                Disposable sendLocationsDisposable
                        = storedLocationsProvider.subscribe(GpsService::sendLocations);
                disposables.add(sendLocationsDisposable);
                break;

            case COMMAND_SAVE_LOCATION:
                LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Error: permission for accessing location not granted", Toast.LENGTH_SHORT).show();
                }
                if (Build.VERSION.SDK_INT < 30) {
                    LocationListener locationListener = new GpsLocationListener();
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
                } else {
                    locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER,
                            null,
                            getMainExecutor(),
                            this::storeLocation);
                }
                break;

            case COMMAND_WIPE_DATA:
                storageManager.wipeData();
                break;

            case COMMAND_SHOW_MIN_MAX_DISTANCE:
                sendMinMaxDistance(storedLocationsProvider.getValue());
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
        super.onDestroy();
    }

    private static void sendLocations(List<MyLocation> myLocations) {
        Intent updateLocationsIntent = new Intent(ACTION_UPDATE_GPS_LOCATIONS);
        String myLocationsJson = new Gson().toJson(myLocations);
        updateLocationsIntent.putExtra(PARAM_LOCATIONS, myLocationsJson);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(updateLocationsIntent);
    }

    private static void sendMinMaxDistance(List<MyLocation> myLocations) {
        Intent sendMinMaxDistanceIntent = new Intent(ACTION_MIN_MAX_DISTANCE);
        sendMinMaxDistanceIntent.putExtra(PARAM_MIN_DISTANCE, Calculator.getMinDistance(myLocations));
        sendMinMaxDistanceIntent.putExtra(PARAM_MAX_DISTANCE, Calculator.getMaxDistance(myLocations));
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(sendMinMaxDistanceIntent);
    }

    private void storeLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        MyLocation myLocation = new MyLocation(latitude, longitude);
        storageManager.addLocation(myLocation);
    }

    private class GpsLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(@NonNull Location location) { storeLocation(location); }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(@NonNull String provider) {}

        @Override
        public void onProviderDisabled(@NonNull String provider) {}
    }
}