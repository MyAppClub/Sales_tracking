package com.company.sales.misc;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.lang.reflect.Type;


public class StorageManager {

    private static String TAG = StorageManager.class.getSimpleName().toString();
    private final static String PREF_NAME = "LocalPref";

    private static SharedPreferences prefs;
    private static SharedPreferences.Editor editor;

    private static void initPrefs(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static Object read(Context context, String key, Type type) {
        try {
            if (prefs == null) {
                initPrefs(context);
            }

            if (key == null) {
                Log.e(TAG, "Empty Key provided to Storage Manager.");
                return false;
            }

            String valueJson = prefs.getString(key, null);


            if (valueJson == null) {
                if (type.equals(Boolean.class)) {
                    return false;
                } else {
                    return null;
                }
            } else {
                Gson gson = new Gson();
                if (type == null) {
                    return gson.fromJson(valueJson, String.class);
                } else {
                    return gson.fromJson(valueJson, type);
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Something went wrong while reading from Storage.", ex);
            return null;
        }
    }

    public static boolean write(Context context, String key, Object value, Type type) {
        try {
            if (prefs == null) {
                initPrefs(context);
            }

            if (key == null) {
                Log.e(TAG, "Empty Key provided to Storage Manager.");
                return false;
            }

            Gson gson = new Gson();
            String valueJson = gson.toJson(value, type);
            editor.putString(key, valueJson);
            return editor.commit();
        } catch (Exception ex) {
            Log.e(TAG, "Something went wrong while writing to Storage.", ex);
            return false;
        }
    }

    public static boolean remove(Context context, String key) {
        try {
            if (prefs == null) {
                initPrefs(context);
            }

            if (key == null) {
                Log.e(TAG, "Empty Key provided to Storage Manager.");
                return false;
            }

            editor.remove(key);
            return editor.commit();
        } catch (Exception ex) {
            Log.e(TAG, "Something went wrong while removing a key from Storage.", ex);
            return false;
        }
    }

    public final static String CACHED_LOCATIONS = "CACHED_LOCATIONS";
    public final static String IS_LOGGED = "IS_LOGGED";
    public final static String TOTAL_DISTANCE = "TOTAL_DISTANCE";

}
