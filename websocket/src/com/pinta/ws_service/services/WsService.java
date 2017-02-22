package com.pinta.ws_service.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import de.tavendo.autobahn.Wamp;
import de.tavendo.autobahn.WampConnection;

import org.greenrobot.eventbus.EventBus;

import com.example.websocket.model.BroadcastMessage;
import com.pinta.ws_service.Consts;
import com.pinta.ws_service.L;
import com.pinta.ws_service.WsManager;
import com.pinta.ws_service.Consts.WsConstant;

public class WsService extends Service {

    private String mWsUri;
    private WampConnection mConnection = new WampConnection();
    private static WsService instance = null;
    private static boolean isClosedByUser = false;
    private static boolean isOpen = false;
    private Handler handler = new Handler();
    
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
                    stopHeartBeatTask();
                }
                
                @Override
                public void onClose(int i, String s) {
                	if (mConnection != null) {
                		log(i + " == " + s);
                        if (!isOpen && i == 2 && WsManager.mHeartBeatPeriodInMillis != 0) {
                        	restartHeartBeatTask();
                        	isOpen = true;
                        	return;
                        }
                        if (i != 2 && WsManager.mHeartBeatPeriodInMillis != 0) {
                        	sendOnClose(s);
                        	isOpen = false;
                        	restartHeartBeatTask();
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
        stopHeartBeatTask();
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
    	BroadcastMessage message = new BroadcastMessage();
    	message.id = WsConstant.WS_CONNECT_OPEN;
    	message.message = null;
    	EventBus.getDefault().post(message);
    }

    private void sendOnClose(String s) {
    	BroadcastMessage message = new BroadcastMessage();
    	message.id = WsConstant.WS_CONNECT_CLOSE;
    	message.message = s;
    	EventBus.getDefault().post(message);
    }
    
    private void sendOnSubscribeEvent(String s) {
    	BroadcastMessage message = new BroadcastMessage();
    	message.id = WsConstant.WS_SUBSCRIBE;
    	message.message = s;
    	//Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    	EventBus.getDefault().post(message);
    }

    private void sendOnUnsubscribeEvent(String s) {
    	BroadcastMessage message = new BroadcastMessage();
    	message.id = WsConstant.WS_UNSUBSCRIBE;
    	message.message = s;
    	EventBus.getDefault().post(message);
    }
    
    private void sendOnCallResult(String s) {
    	BroadcastMessage message = new BroadcastMessage();
    	message.id = WsConstant.WS_CALL;
    	message.message = s;
    	EventBus.getDefault().post(message);
    }

    private void sendOnCallError(String s) {
    	BroadcastMessage message = new BroadcastMessage();
    	message.id = WsConstant.WS_CALL;
    	message.message = s;
    	EventBus.getDefault().post(message);
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
    
    Runnable runnable = new Runnable() {
	    @Override
	    public void run() {
	    	doHeartBeatTask();
	    	handler.postDelayed(this, WsManager.mHeartBeatPeriodInMillis);
	    }
	};
	
    public void stopHeartBeatTask() {
    	handler.removeCallbacks(runnable);
    }
    
    public void restartHeartBeatTask() {
    	handler.removeCallbacks(runnable);
    	handler.post(runnable);
    }
    
    private void doHeartBeatTask() {
    	wsConnect();
    }
}
