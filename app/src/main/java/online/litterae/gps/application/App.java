package online.litterae.gps.application;

import android.app.Application;

import androidx.room.Room;

import online.litterae.gps.storage.MyDatabase;

public class App extends Application {
    private static App app;
    private static MyDatabase database;
    private static final String DATABASE_NAME = "MyDatabase";

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        database = Room.databaseBuilder(this, MyDatabase.class, DATABASE_NAME).build();
    }

    public static App getApp() {
        return app;
    }

    public static MyDatabase getDatabase() {
        return database;
    }
}