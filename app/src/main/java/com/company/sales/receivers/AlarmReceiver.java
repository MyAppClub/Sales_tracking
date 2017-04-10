package com.company.sales.receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.company.sales.misc.App;
import com.company.sales.misc.Utils;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar c = Utils.getCalendarInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek != 1) {
            App.getInstance().logoutUser(App.getInstance().getCurrentActivity());
        }
    }
}
