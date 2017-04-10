package com.company.sales.misc;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static Calendar getCalendarInstance() {
        return Calendar.getInstance();
    }

    public static void showAppLog(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void showToast(final Activity activity, final String message, final int duration) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, duration).show();
            }
        });
    }

    public static Boolean isDuringWorkingHors() {

        Calendar c = getCalendarInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        Log.e(TAG, "Day" + dayOfWeek + "Hour" + hourOfDay + "Minute" + minute);
        boolean valid = dayOfWeek >= 2 && dayOfWeek <= 7 && hourOfDay >= 9 && hourOfDay <= 19;
        if (valid)
            if (hourOfDay == 19)
                return minute <= 30;

        return true;
    }

    public static String dateToISO8601TS(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        return dateFormat.format(date);
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    /*
    public static AlertDialog createTGAlertDialog(final Activity activity, final String title, final String
            message) {

        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View alertView = layoutInflater.inflate(R.layout.dialog_ok_layout, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setCancelable(true).setView(alertView);

        TextView titleTV = (TextView) alertView.findViewById(R.id.title);
        titleTV.setText(title);
        TextView messageTV = (TextView) alertView.findViewById(R.id.message);
        messageTV.setText(message);
        Button okBtn = (Button) alertView.findViewById(R.id.okButton);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        return alertDialog;
    }*/


}
