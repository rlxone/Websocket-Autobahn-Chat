package com.example.websocket.library;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BackgroundManager implements Application.ActivityLifecycleCallbacks {

    private static final String LOG = BackgroundManager.class.getSimpleName();

    public static final long BACKGROUND_DELAY = 500;

    private static BackgroundManager sInstance;

    public interface Listener {
        public void onBecameForeground();
        public void onBecameBackground();
    }

    private boolean mInBackground = true;
    private final List<Listener> listeners = new ArrayList<Listener>();
    private final Handler mBackgroundDelayHandler = new Handler();
    private Runnable mBackgroundTransition;

    public static BackgroundManager get(Application application) {
        if (sInstance == null) {
            sInstance = new BackgroundManager(application);
        }
        return sInstance;
    }

    private BackgroundManager(Application application) {
        application.registerActivityLifecycleCallbacks(this);
    }

    public void registerListener(Listener listener) {
    	listeners.clear();
        listeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isInBackground() {
        return mInBackground;
    }

    public void onActivityResumed(Activity activity) {
        if (mBackgroundTransition != null) {
            mBackgroundDelayHandler.removeCallbacks(mBackgroundTransition);
            mBackgroundTransition = null;
        }

        if (mInBackground) {
            mInBackground = false;
            notifyOnBecameForeground();
            Log.i(LOG, "Application went to foreground");
        }
    }

    private void notifyOnBecameForeground() {
        for (Listener listener : listeners) {
            try {
                listener.onBecameForeground();
            } catch (Exception e) {
                Log.e(LOG, "Listener threw exception!" + e);
            }
        }
    }

    public void onActivityPaused(Activity activity) {
        if (!mInBackground && mBackgroundTransition == null) {
            mBackgroundTransition = new Runnable() {
                public void run() {
                    mInBackground = true;
                    mBackgroundTransition = null;
                    notifyOnBecameBackground();
                    Log.i(LOG, "Application went to background");
                }
            };
            mBackgroundDelayHandler.postDelayed(mBackgroundTransition, BACKGROUND_DELAY);
        }
    }

    private void notifyOnBecameBackground() {
        for (Listener listener : listeners) {
            try {
                listener.onBecameBackground();
            } catch (Exception e) {
                Log.e(LOG, "Listener threw exception!" + e);
            }
        }
    }

    public void onActivityStopped(Activity activity) {}

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    public void onActivityStarted(Activity activity) {}

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    public void onActivityDestroyed(Activity activity) {}
}