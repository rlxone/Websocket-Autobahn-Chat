package com.pinta.ws_service.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import de.tavendo.autobahn.Wamp;
import de.tavendo.autobahn.WampConnection;

import com.pinta.ws_service.Consts;
import com.pinta.ws_service.L;
import com.pinta.ws_service.WsManager;
import com.pinta.ws_service.receivers.HeartBeatTaskReceiver;

public class WsService extends Service {

    private HeartBeatTaskReceiver mHeartBeatTaskReceiver = new HeartBeatTaskReceiver();
    private String mWsUri;
    private WampConnection mConnection = new WampConnection();
    private static WsService instance = null;
    private static boolean isClosedByUser = false;
    private static boolean isOpen = false;
    //private static boolean isFirstConnection = false;
    
    public static boolean isCreated() {
    	return instance != null;
    }
    
    public static boolean isClosedByUser() {
    	return isClosedByUser;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra(Consts.WsConstant.WS_CONNECT)) {
            mWsUri = intent.getStringExtra(Consts.WsConstant.WS_CONNECT);
            wsConnect();
        } else if (intent.hasExtra(Consts.WsConstant.WS_RECONNECT)) {
            wsConnect();
        } else if (intent.hasExtra(Consts.WsConstant.WS_DISCONNECT)) {
            wsDisconnect();
        } else if (intent.hasExtra(Consts.WsConstant.WS_PUBLISH)) {
            String topicUri = intent.getStringExtra(Consts.WsConstant.WS_PUBLISH_TOPIC);
            String message = intent.getStringExtra(Consts.WsConstant.WS_PUBLISH);
            wsPublish(topicUri, message);
        } else if (intent.hasExtra(Consts.WsConstant.WS_SUBSCRIBE)) {
            String topicUri = intent.getStringExtra(Consts.WsConstant.WS_SUBSCRIBE);
            wsSubscribe(topicUri);
        } else if (intent.hasExtra(Consts.WsConstant.WS_CALL)) {
            String topicUri = intent.getStringExtra(Consts.WsConstant.WS_CALL);
            String param = intent.getStringExtra(Consts.WsConstant.WS_CALL_PARAM);
            wsCall(topicUri, param);
        } else if (intent.hasExtra(Consts.WsConstant.WS_UNSUBSCRIBE)) {
            String topicUri = intent.getStringExtra(Consts.WsConstant.WS_UNSUBSCRIBE);
            wsUnsubscribe(topicUri);
        }
        return START_NOT_STICKY;
    }

    private void wsConnect() {
    	isClosedByUser = false;
    	if (!mConnection.isConnected()) {
    		mConnection.connect(mWsUri, new Wamp.ConnectionHandler() {
                @Override
                public void onOpen() {
                    log("open");
                    sendOnOpen();
                    mHeartBeatTaskReceiver.stopHeartBeatTask(WsService.this);
                }
                
                @Override
                public void onClose(int i, String s) {
                	if (mConnection != null) {
                		log(i + " == " + s);
                        if (!isOpen && i == 2 && WsManager.mHeartBeatPeriodInMillis != 0) {
                        	mHeartBeatTaskReceiver.restartHeartBeatTask(WsService.this);
                        	isOpen = true;
                        	return;
                        }
                        if (i != 2 && WsManager.mHeartBeatPeriodInMillis != 0) {
                        	sendOnClose(s);
                        	isOpen = false;
                        	mHeartBeatTaskReceiver.restartHeartBeatTask(WsService.this);
                        }	
                	}
                }
            });	
    	}
    }
    
    private void wsUnsubscribe(String topicName) {
        if (mConnection.isConnected()) {
            mConnection.unsubscribe(topicName);
            sendOnUnsubscribeEvent(topicName);
        }
    }
    
    private void wsDisconnect() {
        if (mConnection.isConnected()) {
            mConnection.disconnect();
        }
        mHeartBeatTaskReceiver.stopHeartBeatTask(WsService.this);
        isClosedByUser = true;
        mConnection = null;
        stopSelf();
    }
    
    private void wsPublish(String topicUri, Object event) {
        if (mConnection.isConnected()) {
            log(event);
            mConnection.publish(topicUri, event);
        }
    }

    private void wsSubscribe(String topicUri) {
        if (mConnection.isConnected()) {
            mConnection.subscribe(topicUri,
                    Object.class,
                    new Wamp.EventHandler() {
                        @Override
                        public void onEvent(String s, Object o) {
                            log(s + " == " + o);
                            sendOnSubscribeEvent(o.toString());
                        }
                    });
        }
    }

    private void wsCall(String topicUri, Object... argument) {
        if (mConnection.isConnected()) {
            mConnection.call(topicUri,
                    Object.class,
                    new Wamp.CallHandler() {
                        @Override
                        public void onResult(Object o) {
                            log(o);
                            sendOnCallResult(o.toString());
                        }

                        @Override
                        public void onError(String s, String s1) {
                            log(s + " == " + s1);
                            sendOnCallError(s1);
                        }
                    },
                    argument);
        }
    }

    private void sendOnOpen() {
    	isOpen = true;
        sendBroadcast(new Intent(Consts.BroadcastConstant.BROADCAST_ACTION_WS)
                .putExtra(Consts.WsConstant.WS_CONNECT_OPEN, "")
        );
    }

    private void sendOnClose(String s) {
        sendBroadcast(new Intent(Consts.BroadcastConstant.BROADCAST_ACTION_WS)
                .putExtra(Consts.WsConstant.WS_CONNECT_CLOSE, s)
        );
    }
    
    private void sendOnSubscribeEvent(String s) {
        sendBroadcast(new Intent(Consts.BroadcastConstant.BROADCAST_ACTION_WS)
                .putExtra(Consts.WsConstant.WS_SUBSCRIBE, s)
        );
    }

    private void sendOnUnsubscribeEvent(String s) {
        sendBroadcast(new Intent(Consts.BroadcastConstant.BROADCAST_ACTION_WS)
                .putExtra(Consts.WsConstant.WS_UNSUBSCRIBE, s)
        );
    }
    
    private void sendOnCallResult(String s) {
        sendBroadcast(new Intent(Consts.BroadcastConstant.BROADCAST_ACTION_WS)
                .putExtra(Consts.WsConstant.WS_CALL, s)
        );
    }

    private void sendOnCallError(String s) {
        sendBroadcast(new Intent(Consts.BroadcastConstant.BROADCAST_ACTION_WS)
                .putExtra(Consts.WsConstant.WS_CALL, s)
        );
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void log(Object o) {
        if (WsManager.mIsLogOn) {
            L.d(o);
        }
    }
    
    @Override
    public void onCreate() {
    	super.onCreate();
    	instance = this;
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	instance = null;
    	isOpen = false;
    }
}
