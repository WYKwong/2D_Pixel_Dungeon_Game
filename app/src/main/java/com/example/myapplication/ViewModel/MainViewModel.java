package com.example.myapplication.ViewModel;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.Model.helper.HelpMethods;

/**
 * Main ViewModel responsible for initializing game display metrics.
 * <p>
 * Memory leak fix: replaced {@code static Context gameContext} (which held
 * a strong reference to the Activity, preventing garbage collection) with
 * {@code static Context appContext} that stores only the Application Context.
 * Application Context is safe to hold statically because it lives for the
 * lifetime of the entire process.
 * </p>
 */
public class MainViewModel extends ViewModel  {

    private static Context appContext;
    private static int gameWidth;
    private static int gameHeight;
    private static double scaleRatio;

    public void initialize(Context context) {
        // Store Application Context instead of Activity to prevent memory leak
        appContext = context.getApplicationContext();

        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRealMetrics(dm);

        gameWidth = dm.widthPixels;
        gameHeight = dm.heightPixels;


        HelpMethods.cleanUi((Activity) context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ((Activity) context).getWindow().getAttributes().layoutInDisplayCutoutMode
                    = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }

    public static Context getGameContext() {
        return appContext;
    }
    public static int getGameWidth() {
        return gameWidth;
    }
    public static int getGameHeight() {
        return gameHeight;
    }
    public static double getScaleRatio(double width, double height) {
        return HelpMethods.getScaleRatio(width, height, gameWidth, gameHeight);
    }

}