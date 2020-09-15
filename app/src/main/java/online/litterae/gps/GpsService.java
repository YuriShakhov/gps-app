package online.litterae.gps;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;

import java.util.List;
import java.util.Objects;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import online.litterae.gps.application.App;
import online.litterae.gps.storage.MyLocation;
import online.litterae.gps.storage.StorageManager;
import online.litterae.gps.utils.Calculator;

import static online.litterae.gps.utils.Const.ACTION_MIN_MAX_DISTANCE;
import static online.litterae.gps.utils.Const.ACTION_UPDATE_GPS_LOCATIONS;
import static online.litterae.gps.utils.Const.COMMAND_CHECK_UPDATE;
import static online.litterae.gps.utils.Const.COMMAND_CONNECT_SERVICE;
import static online.litterae.gps.utils.Const.COMMAND_SAVE_LOCATION;
import static online.litterae.gps.utils.Const.COMMAND_SHOW_MIN_MAX_DISTANCE;
import static online.litterae.gps.utils.Const.COMMAND_WIPE_DATA;
import static online.litterae.gps.utils.Const.NOTIFICATION_CHANNEL_ID;
import static online.litterae.gps.utils.Const.NOTIFICATION_CHANNEL_NAME;
import static online.litterae.gps.utils.Const.NOTIFICATION_CONTENT_TITLE;
import static online.litterae.gps.utils.Const.NOTIFICATION_ID;
import static online.litterae.gps.utils.Const.PARAM_COMMAND;
import static online.litterae.gps.utils.Const.PARAM_LOCATIONS;
import static online.litterae.gps.utils.Const.PARAM_MAX_DISTANCE;
import static online.litterae.gps.utils.Const.PARAM_MIN_DISTANCE;

public class GpsService extends Service {
    private StorageManager storageManager = StorageManager.getInstance();
    private BehaviorSubject<List<MyLocation>> storedLocationsProvider = storageManager.getLocationsProvider();
    private Disposable sendLocationsDisposable;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = intent.getIntExtra(PARAM_COMMAND, 0);
        switch (command) {
            case COMMAND_CONNECT_SERVICE:
                sendLocationsDisposable = storedLocationsProvider.subscribe(this::sendLocations);
                startForegroundService();
                break;

            case COMMAND_SAVE_LOCATION:
                LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                LocationListener locationListener = new GpsLocationListener();
                Objects.requireNonNull(locationManager).requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
                break;

            case COMMAND_WIPE_DATA:
                storageManager.wipeData();
                break;

            case COMMAND_SHOW_MIN_MAX_DISTANCE:
                sendMinMaxDistance(storedLocationsProvider.getValue());
                break;

            case COMMAND_CHECK_UPDATE:
                sendLocationsDisposable.dispose();
                sendLocationsDisposable = storedLocationsProvider.subscribe(this::sendLocations);
                break;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        sendLocationsDisposable.dispose();
        super.onDestroy();
    }

    private void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Objects.requireNonNull(manager).createNotificationChannel(channel);

        Notification.Builder notificationBuilder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_gps_foreground)
                .setContentTitle(NOTIFICATION_CONTENT_TITLE)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void sendLocations(List<MyLocation> myLocations) {
        Intent updateLocationsIntent = new Intent(ACTION_UPDATE_GPS_LOCATIONS);
        String myLocationsJson = new Gson().toJson(myLocations);
        updateLocationsIntent.putExtra(PARAM_LOCATIONS, myLocationsJson);
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(updateLocationsIntent);
    }

    private void sendMinMaxDistance(List<MyLocation> myLocations) {
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