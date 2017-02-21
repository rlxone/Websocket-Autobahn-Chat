package com.pinta.ws_service;

import com.example.websocket.BuildConfig;

import android.util.Log;

public class L {
    private static boolean isDebug = true;
    private static String tagL = "Websocket";

    public static void d(Object msg) {
        if (isDebug) {
            Log.d(tagL, "....." + msg);
        }
    }
}
