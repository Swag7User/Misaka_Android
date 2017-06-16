package ch.uzh;

/**
 * Created by Jesus on 19.04.2017.
 */

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import ch.uzh.helper.App;
import ch.uzh.helper.NativeLoader;

public class GlobalState extends Application {
    private MainWindow mainWindow;
    private static GlobalState Instance;
    public static volatile Handler applicationHandler = null;

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public void setMainWindow(MainWindow mainWindow){
        this.mainWindow = mainWindow;
    }



    @Override
    public void onCreate() {
        super.onCreate();

        Instance=this;

        applicationHandler = new Handler(getInstance().getMainLooper());

        NativeLoader.initNativeLibs(App.getInstance());

    }

    public static GlobalState getInstance()
    {
        return Instance;
    }
}
