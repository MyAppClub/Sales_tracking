package com.company.sales.misc;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.company.sales.activities.LoginActivity;
import com.google.android.gms.location.LocationSettingsRequest;


public class App extends Application {

    private String TAG = App.class.getSimpleName().toString();
    private static App mInstance;
    private Activity mCurrentActivity;

    private LocationSettingsRequest.Builder builder;
    public int TOTAL_TIME = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static App getInstance() {
        return mInstance;
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }


    public void logoutUser(final Activity activity) {
        Log.i(TAG, "logoutUser: signing out...");
        clearUserData();
        goToLoginActivity(activity);
    }

    public void clearUserData() {
        StorageManager.remove(App.getInstance().getCurrentActivity(), StorageManager.IS_LOGGED);
    }

    public void goToLoginActivity(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            activity.finishAffinity();
        }
        activity.startActivity(intent);
    }
}
