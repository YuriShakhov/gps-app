package online.litterae.gps.application;

import android.app.Application;

import androidx.room.Room;

import online.litterae.gps.storage.MyDatabase;

import static online.litterae.gps.utils.Const.DATABASE_NAME;

public class App extends Application {
    private static App app;
    private static MyDatabase myDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        myDatabase = Room.databaseBuilder(this, MyDatabase.class, DATABASE_NAME).build();
    }

    public static App getApp() {
        return app;
    }

    public static MyDatabase getDatabase() {
        return myDatabase;
    }
}