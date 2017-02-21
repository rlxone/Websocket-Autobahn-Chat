package com.pinta.ws_service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import com.pinta.ws_service.services.WsService;

public class WsManager {

    public static boolean mIsLogOn;
    public static long mHeartBeatPeriodInMillis;
    private static WsManager sInstance;
    private String mPort;

    private WsManager() {
        sInstance = this;
    }

    public static WsManager getWsManager() {
        if (sInstance == null) {
            sInstance = new WsManager();
        }
        return sInstance;
    }

    public void disconnect(Context context) {
        context.startService(new Intent(context, WsService.class)
                .putExtra(Consts.WsConstant.WS_DISCONNECT, ""));
        if (EventBus.getDefault().isRegistered(context)) {
        	EventBus.getDefault().unregister(context);
        }
    }

    public void subscribe(Context context, String topicName) {
        context.startService(new Intent(context, WsService.class)
                .putExtra(Consts.WsConstant.WS_SUBSCRIBE, topicName));
    }

    public void call(Context context, String topicName, String param) {
        context.startService(new Intent(context, WsService.class)
                .putExtra(Consts.WsConstant.WS_CALL, topicName)
                .putExtra(Consts.WsConstant.WS_CALL_PARAM, param));
    }

    public void publish(Context context, String topicName, String msg) {
        context.startService(new Intent(context, WsService.class)
                .putExtra(Consts.WsConstant.WS_PUBLISH, msg)
                .putExtra(Consts.WsConstant.WS_PUBLISH_TOPIC, topicName));
    }
    
    public void unsubscribe(Context context, String topicName) {
        context.startService(new Intent(context, WsService.class)
                .putExtra(Consts.WsConstant.WS_UNSUBSCRIBE, topicName));
    }
    
    public WsManager setPort(@NonNull String port) {
        mPort = port;
        return this;
    }

    public WsManager setLog(boolean isLogOn) {
        WsManager.mIsLogOn = isLogOn;
        return this;
    }

    public WsManager setHeartBeat(long heartBeatPeriodInMillis) {
        WsManager.mHeartBeatPeriodInMillis = heartBeatPeriodInMillis;
        return this;
    }

    public void connect(Context context) {
        if (!TextUtils.isEmpty(mPort)) {
            context.startService(new Intent(context, WsService.class)
                    .putExtra(Consts.WsConstant.WS_CONNECT, mPort));
        } else {
            throw new IllegalArgumentException("You must enter ws mPort to connect to.");
        }
    }

    public interface WsCallbackListeners {
        void onWsOpenCallbackListener();
        void onWsCloseCallbackListener(String onCloseMessage);
        void onWsSubscribeCallbackListener(String onSubscribeMessage);
        void onWsCallCallbackListener(String onCallMessage);
        void onWsUnSubscribeCallbackListener(String onUnSubscribeMessage);
    }
}