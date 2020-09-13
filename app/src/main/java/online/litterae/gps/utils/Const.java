package online.litterae.gps.utils;

public class Const {
    public static final String ACTION_UPDATE_GPS_LOCATIONS = "online.litterae.gps.updateLocations";
    public static final String ACTION_MIN_MAX_DISTANCE = "online.litterae.gps.minMaxDistance";

    public static final int COMMAND_SAVE_LOCATION = 1;
    public static final int COMMAND_WIPE_DATA = 2;
    public static final int COMMAND_SHOW_MIN_MAX_DISTANCE = 3;
    public static final int COMMAND_CONNECT_SERVICE = 4;

    public static final double LOCATION_GIVEN_LATITUDE = 55.724599;
    public static final double LOCATION_GIVEN_LONGITUDE = 37.633583;

    public static final int LOCATIONS_EMPTY = 1;
    public static final int LOCATIONS_SAVED = 2;

    public static final String PARAM_COMMAND = "Command";
    public static final String PARAM_LOCATIONS = "Locations";
    public static final String PARAM_MIN_DISTANCE = "MinDistance";
    public static final String PARAM_MAX_DISTANCE = "MaxDistance";
    public static final String PARAM_SERVICE_STATUS = "ServiceStatus";

    public static final int REQUEST_CODE_PERMISSION_LOCATION = 1;

    public static final int SERVICE_DISCONNECTED = 1;
    public static final int SERVICE_CONNECTED = 2;
    public static final int SERVICE_SAVING_LOCATION = 3;

    public static final String TAG = "MyTag";

    public static final String TEXT_ADDING_LOCATION = "Adding new location...";
    public static final String TEXT_CONNECT = "Connect";
    public static final String TEXT_DISCONNECT = "Disconnect";
    public static final String TEXT_SERVICE_CONNECTED = "GPS service connected";
    public static final String TEXT_SERVICE_DISCONNECTED = "GPS service disconnected";
}