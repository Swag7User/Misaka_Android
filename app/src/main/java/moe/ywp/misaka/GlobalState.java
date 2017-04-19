package moe.ywp.misaka;

/**
 * Created by Jesus on 19.04.2017.
 */

import android.app.Application;

public class GlobalState extends Application {
    private MainWindow mainWindow;

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public void setMainWindow(MainWindow mainWindow){
        this.mainWindow = mainWindow;
    }
}
