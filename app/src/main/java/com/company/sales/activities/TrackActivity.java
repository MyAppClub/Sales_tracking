package com.company.sales.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.company.sales.R;
import com.company.sales.databinding.ActivityTrackBinding;
import com.company.sales.entities.SLocation;
import com.company.sales.misc.App;
import com.company.sales.misc.Constants;
import com.company.sales.misc.StorageManager;
import com.company.sales.misc.Utils;
import com.company.sales.services.LocationUpdatesService;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

public class TrackActivity extends SBaseActivity implements View.OnClickListener {
    private static String TAG = TrackActivity.class.getSimpleName().toString();
    ActivityTrackBinding binding;
    private int REQUEST_CHECK_SETTINGS = 10;
    private Location mPreviousLocation;
    private Resources mResources;

    private MyReceiver myReceiver;

    private LocationUpdatesService mService = null;

    private boolean mBound = false;


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_track);
        binding.activityTrackStart.setOnClickListener(this);
        mResources = getResources();
        myReceiver = new MyReceiver();

    }

    @Override
    protected void onStart() {
        super.onStart();
        updateDistanceUI();
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        /*if (checkPermissions()) {
            Utils.showToast(this, mResources.getString(R.string.location_permission_not_granted), Toast.LENGTH_SHORT);
            finish();
        }*/

        if (Utils.isDuringWorkingHors() && Utils.isLocationEnabled(this)) {
            bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        } else if (Utils.isDuringWorkingHors() && !Utils.isLocationEnabled(this)) {
            Utils.showToast(this, mResources.getString(R.string.gps_not_enabled_msg), Toast.LENGTH_SHORT);
            finish();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
    }

    /**
     * Receiver for broadcasts sent by  LocationUpdatesService.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);

            if (location != null) {
                if (location.getAccuracy() > Constants.ACCURACY_THRESHOLD) {
                    return;
                }

                SLocation sLocation = new SLocation();
                sLocation.setLat(location.getLatitude());
                sLocation.setLon(location.getLongitude());
                sLocation.setSpeed(location.getSpeed());
                sLocation.setAccuracy(location.getAccuracy());
                sLocation.setTimestamp(Utils.dateToISO8601TS(new Date()));
                if (mPreviousLocation != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    double d = distanceBetween(mPreviousLocation.getLatitude(), mPreviousLocation.getLongitude(), latitude, longitude);
                }
                mPreviousLocation = location;
                postLocation(sLocation);

                int total = App.getInstance().TOTAL_TIME;
                if (mPreviousLocation != null) {
                    if (mPreviousLocation.getLatitude() == location.getLatitude() && mPreviousLocation.getLongitude() == location.getLongitude() && total < Constants.FIXED_LOCATION_INTERVAL) {
                        total = total + Constants.LOCATION_UPDATE_DELAY;
                        App.getInstance().TOTAL_TIME = total;
                    } else if (mPreviousLocation.getLatitude() == location.getLatitude() && mPreviousLocation.getLongitude() == location.getLongitude() && total == Constants.FIXED_LOCATION_INTERVAL) {
                        App.getInstance().TOTAL_TIME = 0;
                        endpointSendStationeryData();
                    }
                }
                mPreviousLocation = location;
                updateDistanceUI();
            }
        }
    }

    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_track_start:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                App.getInstance().logoutUser(TrackActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateDistanceUI() {
        final String distance = (String) StorageManager.read(this, StorageManager.TOTAL_DISTANCE, String.class);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (distance != null)
                    binding.activityTrackDistanceValue.setText(Constants.DISTANCE + Constants.COLON + Double.parseDouble(distance));
                else
                    binding.activityTrackDistanceValue.setText(Constants.DISTANCE + Constants.COLON + Constants.INITIAL_DISTANCE);
            }
        });
    }

    private void endpointSendStationeryData() {
        /*
            Hit the end point to send stationery data to server
            @POST /stationery
            @apiParam location object
        */
    }

    private void postLocation(final SLocation location) {
        /*
            Hit the end point to send location to server
            @POST /locations
            @apiParam location object
        */
         /*
         if sucess continue and call post location batch
         else call cache location
          */

    }

    private void postLocationBatch(ArrayList<SLocation> locations) {
        /*
            Hit the end point to multiple locations to server
            @POST /location
            @apiParam location object
        */

         /*
         if suucess clear the cached location flag
         else call cache location
          */
    }

    private boolean cacheLocation(SLocation location) {
        Type type = new TypeToken<ArrayList<SLocation>>() {
        }.getType();
        ArrayList<SLocation> locations = (ArrayList) StorageManager.read(this, StorageManager
                .CACHED_LOCATIONS, type);
        if (locations == null) {
            locations = new ArrayList<>();
        }
        locations.add(location);
        return StorageManager.write(this, StorageManager.CACHED_LOCATIONS, locations, type);
    }

    private void pushCachedLocations() {
        Type type = new TypeToken<ArrayList<SLocation>>() {
        }.getType();
        ArrayList<SLocation> locations = (ArrayList) StorageManager.read(this, StorageManager
                .CACHED_LOCATIONS, type);
        if (locations == null) {
            return;
        }
        postLocationBatch(locations);
    }

    //GETS THE DISTANCE BETWEEN TWO LOCATION
    private float distanceBetween(double initialLat, double initialLong, double finalLat, double finalLong) {

        Double d = 0.0;
        if (initialLat != 0 || initialLong != 0 || finalLat != 0 || finalLong != 0) {
            float[] result = new float[4];

            Location.distanceBetween(initialLat, initialLong, finalLat, finalLong, result);
            String distance = (String) StorageManager.read(this, StorageManager.TOTAL_DISTANCE, String.class);
            if (distance != null)
                d = Double.parseDouble(distance);

            d = d + result[0];
            StorageManager.write(this, StorageManager.TOTAL_DISTANCE, d.toString(), String.class);
            return result[0];
        }
        return 0;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "onActivityResult: " + "success");
//                App.getInstance().locationEnabled = true;
            }
        }
    }

}
