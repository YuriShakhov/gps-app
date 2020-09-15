package online.litterae.gps;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import online.litterae.gps.storage.MyLocation;

import static online.litterae.gps.utils.Const.ACTION_MIN_MAX_DISTANCE;
import static online.litterae.gps.utils.Const.ACTION_UPDATE_GPS_LOCATIONS;
import static online.litterae.gps.utils.Const.COMMAND_CHECK_UPDATE;
import static online.litterae.gps.utils.Const.COMMAND_CONNECT_SERVICE;
import static online.litterae.gps.utils.Const.COMMAND_SAVE_LOCATION;
import static online.litterae.gps.utils.Const.COMMAND_SHOW_MIN_MAX_DISTANCE;
import static online.litterae.gps.utils.Const.COMMAND_WIPE_DATA;
import static online.litterae.gps.utils.Const.ERROR_NO_PERMISSION;
import static online.litterae.gps.utils.Const.LOCATIONS_EMPTY;
import static online.litterae.gps.utils.Const.LOCATIONS_SAVED;
import static online.litterae.gps.utils.Const.PARAM_COMMAND;
import static online.litterae.gps.utils.Const.PARAM_LOCATIONS;
import static online.litterae.gps.utils.Const.PARAM_MAX_DISTANCE;
import static online.litterae.gps.utils.Const.PARAM_MIN_DISTANCE;
import static online.litterae.gps.utils.Const.PARAM_SERVICE_STATUS;
import static online.litterae.gps.utils.Const.REQUEST_CODE_PERMISSION_LOCATION;
import static online.litterae.gps.utils.Const.SERVICE_CONNECTED;
import static online.litterae.gps.utils.Const.SERVICE_DISCONNECTED;
import static online.litterae.gps.utils.Const.SERVICE_SAVING_LOCATION;
import static online.litterae.gps.utils.Const.TEXT_ADDING_LOCATION;
import static online.litterae.gps.utils.Const.TEXT_CONNECT;
import static online.litterae.gps.utils.Const.TEXT_DISCONNECT;
import static online.litterae.gps.utils.Const.TEXT_NO_SAVED_LOCATIONS;
import static online.litterae.gps.utils.Const.TEXT_SERVICE_CONNECTED;
import static online.litterae.gps.utils.Const.TEXT_SERVICE_DISCONNECTED;

public class MainActivity extends AppCompatActivity {
    private List<MyLocation> locationList = new ArrayList<>();
    private int serviceStatus = SERVICE_DISCONNECTED;

    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver locationsReceiver;
    private BroadcastReceiver minMaxDistanceReceiver;

    private Type locationsListType = new TypeToken<List<MyLocation>>() {}.getType();
    private Gson gson = new Gson();

    private RecyclerView recycler;
    private LocationsAdapter adapter;
    private Button connectButton;
    private Button saveButton;
    private Button wipeButton;
    private Button distanceButton;
    private TextView statusText;
    private TextView noLocationsTextView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_portrait);
        } else {
            setContentView(R.layout.activity_landscape);
        }

        statusText = findViewById(R.id.tv_status);
        progressBar = findViewById(R.id.progress_bar);
        noLocationsTextView = findViewById(R.id.no_locations_textview);

        recycler = findViewById(R.id.locations_list);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LocationsAdapter();
        recycler.setAdapter(adapter);

        connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener(v -> connectOrDisconnectService());

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> saveLocation());

        wipeButton = findViewById(R.id.wipe_button);
        wipeButton.setOnClickListener(v -> wipeData());

        distanceButton = findViewById(R.id.distance_button);
        distanceButton.setOnClickListener(v -> getMinMaxDistance());

        showServiceStatus(serviceStatus);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        serviceStatus = savedInstanceState.getInt(PARAM_SERVICE_STATUS);
        String locationListJson = savedInstanceState.getString(PARAM_LOCATIONS);

        locationList = gson.fromJson(locationListJson, locationsListType);

        showUpdatedLocations();
        showServiceStatus(serviceStatus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<MyLocation> receivedLocations =
                        gson.fromJson(intent.getStringExtra(PARAM_LOCATIONS), locationsListType);
                if (serviceStatus == SERVICE_SAVING_LOCATION
                        && receivedLocations != null
                        && receivedLocations.size() > locationList.size()
                ) {
                    showServiceStatus(serviceStatus = SERVICE_CONNECTED);
                }
                locationList = receivedLocations;
                showUpdatedLocations();
            }
        };
        minMaxDistanceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int minDistance = intent.getIntExtra(PARAM_MIN_DISTANCE, -2);
                int maxDistance = intent.getIntExtra(PARAM_MAX_DISTANCE, -2);
                showMinMaxDistance(minDistance, maxDistance);
            }
        };
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(
                locationsReceiver, new IntentFilter(ACTION_UPDATE_GPS_LOCATIONS)
        );
        broadcastManager.registerReceiver(
                minMaxDistanceReceiver, new IntentFilter(ACTION_MIN_MAX_DISTANCE)
        );
        if (serviceStatus != SERVICE_DISCONNECTED) {
            checkUpdate();
        }
    }

    @Override
    protected void onPause() {
        broadcastManager.unregisterReceiver(locationsReceiver);
        broadcastManager.unregisterReceiver(minMaxDistanceReceiver);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(PARAM_SERVICE_STATUS, serviceStatus);
        String locationListJson = gson.toJson(locationList);
        outState.putString(PARAM_LOCATIONS, locationListJson);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGpsService();
            } else {
                showToast(ERROR_NO_PERMISSION);
            }
        }
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION_LOCATION);
    }

    private boolean areLocationPermissionsGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void connectOrDisconnectService() {
        if (serviceStatus == SERVICE_DISCONNECTED) {
            if (areLocationPermissionsGranted()) {
                startGpsService();
            } else {
                requestLocationPermissions();
            }
        } else {
            stopGpsService();
        }
    }

    private void startGpsService() {
        Intent connectIntent = new Intent(this, GpsService.class);
        connectIntent.putExtra(PARAM_COMMAND, COMMAND_CONNECT_SERVICE);
        startService(connectIntent);
        showServiceStatus(serviceStatus = SERVICE_CONNECTED);
    }

    private void stopGpsService() {
        stopService(new Intent(this, GpsService.class));
        showServiceStatus(serviceStatus = SERVICE_DISCONNECTED);
    }

    private void saveLocation() {
        Intent saveLocationIntent = new Intent(this, GpsService.class);
        saveLocationIntent.putExtra(PARAM_COMMAND, COMMAND_SAVE_LOCATION);
        startService(saveLocationIntent);
        showServiceStatus(serviceStatus = SERVICE_SAVING_LOCATION);
    }

    private void wipeData() {
        Intent wipeDataIntent = new Intent(this, GpsService.class);
        wipeDataIntent.putExtra(PARAM_COMMAND, COMMAND_WIPE_DATA);
        startService(wipeDataIntent);
    }

    private void getMinMaxDistance() {
        Intent distanceIntent = new Intent(this, GpsService.class);
        distanceIntent.putExtra(PARAM_COMMAND, COMMAND_SHOW_MIN_MAX_DISTANCE);
        startService(distanceIntent);
    }

    private void checkUpdate() {
        Intent updateIntent = new Intent(this, GpsService.class);
        updateIntent.putExtra(PARAM_COMMAND, COMMAND_CHECK_UPDATE);
        startService(updateIntent);
    }

    public void showUpdatedLocations(){
        adapter.notifyDataSetChanged();
        if (locationList.isEmpty()) {
            showDataStatus(LOCATIONS_EMPTY);
        } else {
            showDataStatus(LOCATIONS_SAVED);
            recycler.smoothScrollToPosition(locationList.size() - 1);
        }
    }

    public void showMinMaxDistance(int minDistance, int maxDistance) {
        if (minDistance >= 0 && maxDistance >= 0) {
            showToast("Minimum distance: " + minDistance + "m, \nmaximum distance: " + maxDistance + "m");
        } else {
            showToast(TEXT_NO_SAVED_LOCATIONS);
        }
    }

    private void showServiceStatus(int serviceStatus) {
        switch (serviceStatus) {
            case SERVICE_DISCONNECTED :
                connectButton.setText(TEXT_CONNECT);
                statusText.setText(TEXT_SERVICE_DISCONNECTED);
                statusText.setTextColor(Color.RED);
                progressBar.setVisibility(View.INVISIBLE);
                saveButton.setEnabled(false);
                wipeButton.setEnabled(false);
                distanceButton.setEnabled(false);
                break;

            case SERVICE_CONNECTED :
                connectButton.setText(TEXT_DISCONNECT);
                statusText.setText(TEXT_SERVICE_CONNECTED);
                statusText.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
                progressBar.setVisibility(View.INVISIBLE);
                saveButton.setEnabled(true);
                break;

            case SERVICE_SAVING_LOCATION :
                statusText.setText(TEXT_ADDING_LOCATION);
                statusText.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
                progressBar.setVisibility(View.VISIBLE);
                saveButton.setEnabled(false);
                break;
        }
    }

    private void showDataStatus(int dataStatus) {
        switch (dataStatus) {
            case LOCATIONS_EMPTY : {
                recycler.setVisibility(View.GONE);
                noLocationsTextView.setVisibility(View.VISIBLE);
                wipeButton.setEnabled(false);
                distanceButton.setEnabled(false);
                break;
            }
            case LOCATIONS_SAVED : {
                recycler.setVisibility(View.VISIBLE);
                noLocationsTextView.setVisibility(View.GONE);
                wipeButton.setEnabled(true);
                distanceButton.setEnabled(true);
                break;
            }
        }
    }

    public static String formatLocation(MyLocation location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        StringBuilder locationSb = new StringBuilder();
        if (longitude >= 0) {
            locationSb.append(longitude).append("E");
        } else {
            locationSb.append(-longitude).append("W");
        }
        locationSb.append(",");
        if (latitude >= 0) {
            locationSb.append(latitude).append("N");
        } else {
            locationSb.append(-latitude).append("S");
        }
        return locationSb.toString();
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.LocationsViewholder> {

        @NonNull
        @Override
        public LocationsViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.location, parent, false);
            return new LocationsViewholder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull LocationsViewholder holder, int position) {
            MyLocation location = locationList.get(position);
            holder.locationText.setText(formatLocation(location));
        }

        @Override
        public int getItemCount() {
            return locationList.size();
        }

        private class LocationsViewholder extends RecyclerView.ViewHolder {
            TextView locationText = itemView.findViewById(R.id.tv_location);

            public LocationsViewholder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}