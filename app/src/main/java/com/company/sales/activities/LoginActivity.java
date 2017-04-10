package com.company.sales.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.company.sales.R;
import com.company.sales.databinding.ActivityLoginBinding;
import com.company.sales.misc.App;
import com.company.sales.misc.Constants;
import com.company.sales.misc.StorageManager;
import com.company.sales.misc.Utils;
import com.company.sales.receivers.AlarmReceiver;

import java.util.Calendar;

public class LoginActivity extends SBaseActivity implements View.OnClickListener {

    private String TAG = LoginActivity.class.getSimpleName();
    private int REQUEST_CHECK_SETTINGS = 10;
    ActivityLoginBinding binding;
    private Resources mResources;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.activityLoginBtn.setOnClickListener(this);
        mResources = getResources();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String isLoggedIn = (String) StorageManager.read(LoginActivity.this, StorageManager.IS_LOGGED, String.class);
        if (Utils.isDuringWorkingHors() && isLoggedIn != null && !isLoggedIn.isEmpty()) {
            initiateAutoLogout();
            Intent i = new Intent(LoginActivity.this, TrackActivity.class);
            startActivity(i);
            finish();
        }
    }

    private Boolean validateInputs() {
        String uname = binding.activityLoginUname.getText().toString();
        String pwd = binding.activityLoginPassword.getText().toString();

        if (uname.trim().length() <= 0) {
            binding.activityLoginUname.setError(mResources.getString(R.string.uname_input_blank_error));
            return false;
        }
        if (pwd.trim().length() <= 0) {
            binding.activityLoginPassword.setError(mResources.getString(R.string.password_input_blank_error));
            return false;
        }
        return true;
    }

    private void endpointLogin() {

        if (Utils.isLocationEnabled(this)) {
            Utils.showToast(LoginActivity.this, mResources.getString(R.string.gps_not_enabled_msg), Toast.LENGTH_SHORT);
            return;
        }

        //Hit the login end point
        String username = binding.activityLoginUname.getText().toString().toLowerCase();
        String password = binding.activityLoginPassword.getText().toString().toLowerCase();

        if (username.equals("username") && password.equals("password")) {
            String sessionToken = "sessionToken";
            StorageManager.write(LoginActivity.this, StorageManager.IS_LOGGED, sessionToken, String.class);
            Intent i = new Intent(LoginActivity.this, TrackActivity.class);
            startActivity(i);
            finish();
        } else {
            Utils.showToast(LoginActivity.this, mResources.getString(R.string.msg_error_connectivity), Toast.LENGTH_LONG);
        }

    }

    public void initiateAutoLogout() {

        Calendar cal = Utils.getCalendarInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, Constants.HOUR_OF_ALARM);
        cal.set(Calendar.MINUTE, Constants.MINUTE_OF_ALARM);

        // Get the AlarmManager service
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(LoginActivity.this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(LoginActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, sender);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_login_btn:
                if (validateInputs())
                    endpointLogin();
                break;
        }
    }

}
